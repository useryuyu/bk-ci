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

package com.tencent.devops.store.service.common.impl

import com.tencent.devops.auth.api.service.ServiceDeptResource
import com.tencent.devops.auth.pojo.DeptInfo
import com.tencent.devops.common.api.constant.CommonMessageCode
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.api.util.UUIDUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.service.utils.MessageCodeUtil
import com.tencent.devops.model.store.tables.records.TStorePublisherInfoRecord
import com.tencent.devops.model.store.tables.records.TStorePublisherMemberRelRecord
import com.tencent.devops.project.api.service.ServiceUserResource
import com.tencent.devops.store.dao.common.PublishersDao
import com.tencent.devops.store.dao.common.StoreDockingPlatformDao
import com.tencent.devops.store.dao.common.StoreMemberDao
import com.tencent.devops.store.pojo.common.PublisherInfo
import com.tencent.devops.store.pojo.common.PublishersRequest
import com.tencent.devops.store.pojo.common.StoreDockingPlatformRequest
import com.tencent.devops.store.pojo.common.enums.PublisherType
import com.tencent.devops.store.pojo.common.enums.StoreTypeEnum
import com.tencent.devops.store.service.common.PublishersDataService
import com.tencent.devops.store.service.common.StoreUserService
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PublishersDataServiceImpl @Autowired constructor(
    private val dslContext: DSLContext,
    private val publishersDao: PublishersDao,
    private val client: Client,
    private val storeDockingPlatformDao: StoreDockingPlatformDao,
    private val storeMemberDao: StoreMemberDao,
    private val storeUserService: StoreUserService
) : PublishersDataService {
    override fun createPublisherData(userId: String, publishers: List<PublishersRequest>): Int {
        val storePublisherInfoRecords = mutableListOf<TStorePublisherInfoRecord>()
        val storePublisherMemberRelRecords = mutableListOf<TStorePublisherMemberRelRecord>()
        publishers.forEach {
            val deptInfos = analysisDept(userId, it.organization)
            if (deptInfos.isEmpty()) {
                logger.error("createPublisherData analysis dept data error!")
                return 0
            }
            val storePublisherInfo = TStorePublisherInfoRecord()
            val storePublisherInfoId = UUIDUtil.generate()
            storePublisherInfo.id = storePublisherInfoId
            storePublisherInfo.publisherCode = it.publishersCode
            storePublisherInfo.publisherName = it.name
            storePublisherInfo.publisherType = it.publishersType.name
            storePublisherInfo.owners = it.owners[0]
            storePublisherInfo.helper = it.helper
            storePublisherInfo.firstLevelDeptId = deptInfos[0].id.toLong()
            storePublisherInfo.firstLevelDeptName = deptInfos[0].name
            storePublisherInfo.secondLevelDeptId = deptInfos[1].id.toLong()
            storePublisherInfo.secondLevelDeptName = deptInfos[1].name
            storePublisherInfo.thirdLevelDeptId = deptInfos[2].id.toLong()
            storePublisherInfo.thirdLevelDeptName = deptInfos[2].name
            if (deptInfos.size > 3) {
                storePublisherInfo.fourthLevelDeptId = deptInfos[3].id.toLong()
                storePublisherInfo.fourthLevelDeptName = deptInfos[3].name
            }
            storePublisherInfo.organizationName = it.organization
            storePublisherInfo.BgName = it.BgName
            storePublisherInfo.certificationFlag = it.certificationFlag
            storePublisherInfo.storeType = it.storeType.type.toByte()
            storePublisherInfo.creator = userId
            storePublisherInfo.modifier = userId
            storePublisherInfo.createTime = LocalDateTime.now()
            storePublisherInfo.updateTime = LocalDateTime.now()
            storePublisherInfoRecords.add(storePublisherInfo)
            if (it.publishersType == PublisherType.ORGANIZATION) {
                //  生成可使用组织发布者进行发布的成员关联
                it.members.forEach { memberId ->
                    val storePublisherMemberRel = TStorePublisherMemberRelRecord()
                    storePublisherMemberRel.id = UUIDUtil.generate()
                    storePublisherMemberRel.publisherId = storePublisherInfoId
                    storePublisherMemberRel.memberId = memberId
                    storePublisherMemberRel.creator = userId
                    storePublisherMemberRel.createTime = LocalDateTime.now()
                    storePublisherMemberRel.modifier = userId
                    storePublisherMemberRel.updateTime = LocalDateTime.now()
                    storePublisherMemberRelRecords.add(storePublisherMemberRel)
                }
            }
        }
        val batchCreateCount = publishersDao.batchCreate(dslContext, storePublisherInfoRecords)
        publishersDao.batchCreatePublisherMemberRel(dslContext, storePublisherMemberRelRecords)
        return batchCreateCount
    }

    override fun updatePublisherData(userId: String, publishers: List<PublishersRequest>): Int {
        val storePublisherInfoRecords = mutableListOf<TStorePublisherInfoRecord>()
        val addStorePublisherMemberRelRecords = mutableListOf<TStorePublisherMemberRelRecord>()
        val delStorePublisherMemberRelRecords = mutableListOf<TStorePublisherMemberRelRecord>()
        publishers.forEach {
            val publisherId = publishersDao.getPublisherId(dslContext, it.publishersCode)
            val deptInfos = analysisDept(userId, it.organization)
            publisherId?.let { id ->
                val records = TStorePublisherInfoRecord()
                records.id = id
                records.publisherCode = it.publishersCode
                records.publisherName = it.name
                records.firstLevelDeptName = deptInfos[0].name
                records.firstLevelDeptId = deptInfos[0].id.toLong()
                records.secondLevelDeptName = deptInfos[1].name
                records.secondLevelDeptId = deptInfos[1].id.toLong()
                records.thirdLevelDeptId = deptInfos[2].id.toLong()
                records.thirdLevelDeptName = deptInfos[2].name
                if (deptInfos.size > 3) {
                    records.fourthLevelDeptId = deptInfos[3].id.toLong()
                    records.fourthLevelDeptName = deptInfos[3].name
                }
                records.publisherType = it.publishersType.name
                records.owners = JsonUtil.toJson(it.owners)
                records.certificationFlag = it.certificationFlag
                records.organizationName = it.organization
                records.modifier = userId
                records.BgName = it.BgName
                records.helper = it.helper
                records.storeType = it.storeType.type.toByte()
                records.updateTime = LocalDateTime.now()
                storePublisherInfoRecords.add(records)
                updateMembers(
                    userId = userId,
                    publisherId = id,
                    newMembers = it.members,
                    addRecords = addStorePublisherMemberRelRecords,
                    delRecords = delStorePublisherMemberRelRecords
                )
            }
        }
        var count = 0
        dslContext.transaction { t ->
            val context = DSL.using(t)
            count = publishersDao.batchUpdate(context, storePublisherInfoRecords)
            publishersDao.batchCreatePublisherMemberRel(context, addStorePublisherMemberRelRecords)
            publishersDao.batchDeletePublisherMemberByMemberIds(context, delStorePublisherMemberRelRecords)
        }
        return count
    }

    override fun deletePublisherData(userId: String, publishers: List<PublishersRequest>): Int {

        val organizePublishers = mutableListOf<String>()
        publishers.map {
            //  获取删除的组织发布者
            if (it.publishersType == PublisherType.ORGANIZATION) {
                organizePublishers.add(it.publishersCode)
            }
        }
        if (organizePublishers.isNotEmpty()) {
            //  删除组织发布者关联的组织成员关联
            val organizePublishersIds = publishersDao.getPublisherIdsByCode(dslContext, organizePublishers)
            publishersDao.batchDeletePublisherMemberRelByPublisherId(dslContext, organizePublishersIds)
        }
        return publishersDao.batchDelete(dslContext, publishers)
    }

    private fun updateMembers(
        userId: String,
        publisherId: String,
        newMembers: List<String>,
        addRecords: MutableList<TStorePublisherMemberRelRecord>,
        delRecords: MutableList<TStorePublisherMemberRelRecord>
    ) {
        val members = publishersDao.getPublisherMemberRelMemberIdsByPublisherId(dslContext, publisherId)
        val intersection = members.intersect(newMembers)
        members.forEach { member ->
            if (!intersection.contains(member)) {
                val storePublisherMemberRel = TStorePublisherMemberRelRecord()
                storePublisherMemberRel.publisherId = publisherId
                storePublisherMemberRel.memberId = member
                delRecords.add(storePublisherMemberRel)
            }
        }
        newMembers.forEach { newMember ->
            if (!intersection.contains(newMember)) {
                val storePublisherMemberRel = TStorePublisherMemberRelRecord()
                storePublisherMemberRel.id = UUIDUtil.generate()
                storePublisherMemberRel.publisherId = publisherId
                storePublisherMemberRel.memberId = newMember
                storePublisherMemberRel.creator = userId
                storePublisherMemberRel.createTime = LocalDateTime.now()
                storePublisherMemberRel.modifier = userId
                storePublisherMemberRel.updateTime = LocalDateTime.now()
                addRecords.add(storePublisherMemberRel)
            }
        }
    }

    override fun createPlatformsData(
        userId: String,
        storeDockingPlatformRequests: List<StoreDockingPlatformRequest>
    ): Int {
        return storeDockingPlatformDao.batchCreate(dslContext, userId, storeDockingPlatformRequests)
    }

    override fun deletePlatformsData(
        userId: String,
        storeDockingPlatformRequests: List<StoreDockingPlatformRequest>
    ): Int {
        return storeDockingPlatformDao.batchDelete(dslContext, userId, storeDockingPlatformRequests)
    }

    override fun updatePlatformsData(
        userId: String,
        storeDockingPlatformRequests: List<StoreDockingPlatformRequest>
    ): Int {
        return storeDockingPlatformDao.batchUpdate(dslContext, userId, storeDockingPlatformRequests)
    }

    override fun getPublishers(
        userId: String,
        storeCode: String,
        storeType: StoreTypeEnum
    ): Result<List<PublisherInfo>> {
        val publishersInfos = mutableListOf<PublisherInfo>()
        if (!storeMemberDao.isStoreMember(
                dslContext = dslContext,
                userId = userId,
                storeCode = storeCode,
                storeType = storeType.type.toByte()
            )) {
            return MessageCodeUtil.generateResponseDataObject(CommonMessageCode.PERMISSION_DENIED)
        }
        val organizationPublisherIds =
            publishersDao.getPublisherMemberRelByMemberId(dslContext, userId)
        if (organizationPublisherIds.isNotEmpty()) {
            // 获取组织发布者信息
            organizationPublisherIds.forEach {
                val organizationPublisherInfo = publishersDao.getPublisherInfoById(dslContext, it)
                publishersInfos.add(organizationPublisherInfo!!)
            }
        }
        var personPublisherInfo = publishersDao.getPublisherInfoByCode(dslContext, userId)
        logger.debug("getPublishers personPublisherInfo is $personPublisherInfo")
        if (personPublisherInfo == null) {
            // 如果未注册发布者则自动注册并返回
            val userDeptInfo = client.get(ServiceUserResource::class).getDetailFromCache(userId).data
            userDeptInfo?.let {
                personPublisherInfo = PublisherInfo(
                    id = UUIDUtil.generate(),
                    publisherCode = userId,
                    publisherName = userId,
                    publisherType = PublisherType.PERSON,
                    owners = userId,
                    helper = userId,
                    firstLevelDeptId = it.bgId.toInt(),
                    firstLevelDeptName = it.bgName,
                    secondLevelDeptId = it.deptId.toInt(),
                    secondLevelDeptName = it.deptName,
                    thirdLevelDeptId = it.centerId.toInt(),
                    thirdLevelDeptName = it.centerName,
                    fourthLevelDeptId = it.groupId.toInt(),
                    fourthLevelDeptName = it.groupName,
                    organizationName = storeUserService.getUserFullDeptName(userId).data ?: "",
                    BgName = it.BgName,
                    certificationFlag = false,
                    storeType = storeType,
                    creator = userId,
                    modifier = userId,
                    createTime = LocalDateTime.now(),
                    updateTime = LocalDateTime.now()
                )
                publishersDao.create(dslContext, personPublisherInfo!!)
            }
        }
        personPublisherInfo?.let { publishersInfos.add(it) }
        logger.debug("getPublishers $publishersInfos")
        return Result(publishersInfos)
    }

    override fun updatePlatformsLogoInfo(userId: String, platformCode: String, logoUrl: String): Boolean {
        storeDockingPlatformDao.getStoreDockingPlatformByCode(dslContext, platformCode)?.let {
            storeDockingPlatformDao.updateStoreDockingPlatformLogoUrl(dslContext, it, logoUrl)
            return true
        }
        return false
    }

    private fun analysisDept(userId: String, organization: String): List<DeptInfo> {
        //  根据解析组织名称获取组织ID
        val deptNames = organization.split("/")
        val deptInfos = mutableListOf<DeptInfo>()
        deptNames.forEachIndexed() { index, deptName ->
            val result = client.get(ServiceDeptResource::class).getDeptByName(userId, deptName).data
            result?.let { it -> deptInfos.add(index, it.results[0]) }
        }
        return deptInfos
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PublishersDataServiceImpl::class.java)
    }
}
