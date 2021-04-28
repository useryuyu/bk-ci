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

package com.tencent.devops.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.mock
import com.tencent.devops.common.redis.RedisOperation
import org.junit.Test

class AuthDeptServiceImplTest{

    private val redisOperation: RedisOperation = mock()

    private val objectMapper: ObjectMapper = mock()

    @Test
    fun test() {
        val authDeptServiceImpl = AuthDeptServiceImpl(redisOperation, objectMapper)
        val response = "{\n" +
            "    \"message\": \"success\",\n" +
            "    \"code\": 0,\n" +
            "    \"data\": {\n" +
            "        \"count\": 162,\n" +
            "        \"results\": [\n" +
            "            {\n" +
            "                \"status\": \"NORMAL\",\n" +
            "                \"domain\": \"XXX.com\",\n" +
            "                \"telephone\": \"XXX\",\n" +
            "                \"create_time\": \"2021-04-27T15:33:59.000000Z\",\n" +
            "                \"country_code\": \"86\",\n" +
            "                \"logo\": null,\n" +
            "                \"iso_code\": \"CN\",\n" +
            "                \"id\": 173169,\n" +
            "                \"display_name\": \"abc\",\n" +
            "                \"leader\": [\n" +
            "                    {\n" +
            "                        \"username\": \"abcv\",\n" +
            "                        \"display_name\": \"abc\",\n" +
            "                        \"id\": 2435\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"username\": \"abc\",\n" +
            "                \"update_time\": \"2021-04-27T15:33:59.000000Z\",\n" +
            "                \"wx_userid\": \"\",\n" +
            "                \"staff_status\": \"IN\",\n" +
            "                \"password_valid_days\": -1,\n" +
            "                \"qq\": \"\",\n" +
            "                \"language\": \"zh-cn\",\n" +
            "                \"enabled\": true,\n" +
            "                \"time_zone\": \"Asia/Shanghai\",\n" +
            "                \"departments\": [\n" +
            "                    {\n" +
            "                        \"order\": 1,\n" +
            "                        \"id\": 6580,\n" +
            "                        \"full_name\": \"123/345\",\n" +
            "                        \"name\": \"应用开发组\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"email\": \"abc@XXX.com\",\n" +
            "                \"extras\": {\n" +
            "                    \"gender\": \"男\",\n" +
            "                    \"postname\": \"应用开发组员工\"\n" +
            "                },\n" +
            "                \"position\": 0,\n" +
            "                \"category_id\": 2\n" +
            "            },\n" +
            "            {\n" +
            "                \"status\": \"NORMAL\",\n" +
            "                \"domain\": \"XXX.com\",\n" +
            "                \"telephone\": \"XXX\",\n" +
            "                \"create_time\": \"2021-04-27T15:33:59.000000Z\",\n" +
            "                \"country_code\": \"86\",\n" +
            "                \"logo\": null,\n" +
            "                \"iso_code\": \"CN\",\n" +
            "                \"id\": 173168,\n" +
            "                \"display_name\": \"abc\",\n" +
            "                \"leader\": [\n" +
            "                    {\n" +
            "                        \"username\": \"abc\",\n" +
            "                        \"display_name\": \"abc\",\n" +
            "                        \"id\": 2435\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"username\": \"def\",\n" +
            "                \"update_time\": \"2021-04-27T15:33:59.000000Z\",\n" +
            "                \"wx_userid\": \"\",\n" +
            "                \"staff_status\": \"IN\",\n" +
            "                \"password_valid_days\": -1,\n" +
            "                \"qq\": \"\",\n" +
            "                \"language\": \"zh-cn\",\n" +
            "                \"enabled\": true,\n" +
            "                \"time_zone\": \"Asia/Shanghai\",\n" +
            "                \"departments\": [\n" +
            "                    {\n" +
            "                        \"order\": 1,\n" +
            "                        \"id\": 6580,\n" +
            "                        \"full_name\": \"123/455\",\n" +
            "                        \"name\": \"应用开发组\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"email\": \"XXX@126.com\",\n" +
            "                \"extras\": {\n" +
            "                    \"gender\": \"男\",\n" +
            "                    \"postname\": \"应用开发组员工\"\n" +
            "                },\n" +
            "                \"position\": 0,\n" +
            "                \"category_id\": 2\n" +
            "            }\n" +
            "        ]\n" +
            "    },\n" +
            "    \"result\": true,\n" +
            "    \"request_id\": \"ca60654f7f054606a56a9644b7211ddf\"\n" +
            "}"
        val users = authDeptServiceImpl.findUserName(response)
        print(users)
    }
}
