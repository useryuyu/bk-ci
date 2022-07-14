/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.process.service.view

import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.model.process.tables.records.TPipelineViewGroupRecord
import com.tencent.devops.model.process.tables.records.TPipelineViewRecord
import com.tencent.devops.process.constant.PipelineViewType
import com.tencent.devops.process.dao.label.PipelineViewDao
import com.tencent.devops.process.dao.label.PipelineViewGroupDao
import com.tencent.devops.process.dao.label.PipelineViewTopDao
import com.tencent.devops.process.engine.dao.PipelineInfoDao
import com.tencent.devops.process.pojo.classify.PipelineNewViewCreate
import com.tencent.devops.process.service.label.PipelineGroupService
import com.tencent.devops.process.service.view.lock.PipelineViewGroupLock
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PipelineViewGroupService @Autowired constructor(
    private val pipelineViewService: PipelineViewService,
    private val pipelineGroupService: PipelineGroupService,
    private val pipelineViewDao: PipelineViewDao,
    private val pipelineViewGroupDao: PipelineViewGroupDao,
    private val pipelineViewTopDao: PipelineViewTopDao,
    private val pipelineInfoDao: PipelineInfoDao,
    private val dslContext: DSLContext,
    private val redisOperation: RedisOperation
) {
    fun addViewGroup(projectId: String, userId: String, pipelineView: PipelineNewViewCreate): Long {
        var viewId = 0L
        dslContext.transaction { t ->
            val context = DSL.using(t)
            viewId = pipelineViewService.addView(userId, projectId, pipelineView, context)
            if (pipelineView.viewType == PipelineViewType.DYNAMIC) {
                initDynamicViewGroup(pipelineViewDao.get(context, projectId, viewId)!!, context)
            } else {
                pipelineViewGroupDao.batchCreate(context, pipelineView.pipelineIds.map {
                    val viewGroup = TPipelineViewGroupRecord()
                    viewGroup.projectId = projectId
                    viewGroup.viewId = viewId
                    viewGroup.pipelineId = it
                    viewGroup.creator = userId
                    viewGroup
                })
            }
        }
        return viewId
    }

    fun listPipelineIdsByViewId(projectId: String, viewId: Long): List<String> {
        val view = pipelineViewDao.get(dslContext, projectId, viewId)
        if (view == null) {
            logger.warn("null view , project:$projectId , view:$viewId")
            return emptyList()
        }
        val isStatic = view.viewType == PipelineViewType.STATIC
        val viewGroups = pipelineViewGroupDao.listByViewId(dslContext, projectId, viewId)
        if (viewGroups.isEmpty()) {
            return if (isStatic) emptyList() else initDynamicViewGroup(view)
        }
        return viewGroups.map { it.pipelineId }.toList()
    }

    fun initDynamicViewGroup(view: TPipelineViewRecord, context: DSLContext? = null): List<String> {
        val projectId = view.projectId
        return PipelineViewGroupLock(redisOperation, projectId).lockAround {
            val firstInit = redisOperation.setIfAbsent("initDynamicViewGroup:$projectId:${view.id}", "1")
            if (!firstInit) {
                return@lockAround emptyList()
            }
            val pipelineInfos = pipelineInfoDao.listInfoByPipelineIds(
                dslContext = context ?: dslContext,
                pipelineIds = setOf(projectId)
            )
            return@lockAround pipelineInfos.asSequence()
                .filter { pipelineViewService.matchView(view, it) }
                .map { it.pipelineId }
                .toList()
        }
    }

    fun updateGroupAfterPipelineCreate(projectId: String, pipelineId: String, userId: String) {
        PipelineViewGroupLock(redisOperation, projectId).lockAround {
            logger.info("updateGroupAfterPipelineCreate, projectId:$projectId, pipelineId:$pipelineId , userId:$userId")
            val pipelineInfo = pipelineInfoDao.getPipelineId(dslContext, projectId, pipelineId)!!
            val viewGroupCount =
                pipelineViewGroupDao.countByPipelineId(dslContext, pipelineInfo.projectId, pipelineInfo.pipelineId)
            if (viewGroupCount == 0) {
                val dynamicProjectViews =
                    pipelineViewDao.list(dslContext, pipelineInfo.projectId, PipelineViewType.DYNAMIC)
                val matchViewIds = dynamicProjectViews.asSequence()
                    .filter { pipelineViewService.matchView(it, pipelineInfo) }
                    .map { it.id }
                    .toSet()
                pipelineViewGroupDao.batchCreate(dslContext, matchViewIds.map {
                    val viewGroup = TPipelineViewGroupRecord()
                    viewGroup.projectId = projectId
                    viewGroup.pipelineId = pipelineId
                    viewGroup.viewId = it
                    viewGroup.creator = userId
                    viewGroup
                })
            }
        }
    }

    fun updateGroupAfterPipelineDelete(projectId: String, pipelineId: String) {
        PipelineViewGroupLock(redisOperation, projectId).lockAround {
            logger.info("updateGroupAfterPipelineDelete, projectId:$projectId, pipelineId:$pipelineId")
            pipelineViewGroupDao.listByPipelineId(dslContext, projectId, pipelineId).forEach {
                pipelineViewGroupDao.remove(dslContext, it.projectId, it.viewId, it.pipelineId)
            }
        }
    }

    fun updateGroupAfterPipelineUpdate(projectId: String, pipelineId: String, userId: String) {
        PipelineViewGroupLock(redisOperation, projectId).lockAround {
            logger.info("updateGroupAfterPipelineUpdate, projectId:$projectId, pipelineId:$pipelineId , userId:$userId")
            val pipelineInfo = pipelineInfoDao.getPipelineId(dslContext, projectId, pipelineId)!!
            // 所有的动态项目组
            val dynamicProjectViews = pipelineViewDao.list(dslContext, pipelineInfo.projectId, PipelineViewType.DYNAMIC)
            val dynamicProjectViewIds = dynamicProjectViews.asSequence()
                .map { it.id }
                .toSet()
            // 命中的动态项目组
            val matchViewIds = dynamicProjectViews.asSequence()
                .filter { pipelineViewService.matchView(it, pipelineInfo) }
                .map { it.id }
                .toSet()
            // 已有的动态项目组
            val baseViewGroups =
                pipelineViewGroupDao.listByPipelineId(dslContext, pipelineInfo.projectId, pipelineInfo.pipelineId)
                    .filter { dynamicProjectViewIds.contains(it.viewId) }
                    .toSet()
            val baseViewIds = baseViewGroups.map { it.viewId }.toSet()
            // 新增新命中的项目组
            pipelineViewGroupDao.batchCreate(dslContext, matchViewIds.filterNot { baseViewIds.contains(it) }.map {
                val viewGroup = TPipelineViewGroupRecord()
                viewGroup.projectId = projectId
                viewGroup.pipelineId = pipelineId
                viewGroup.viewId = it
                viewGroup.creator = userId
                viewGroup
            })
            // 删除未命中的老项目组
            baseViewGroups.filterNot { matchViewIds.contains(it.viewId) }.forEach {
                pipelineViewGroupDao.remove(dslContext, it.projectId, it.viewId, it.pipelineId)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PipelineViewGroupService::class.java)
    }
}
