<template>
    <section>
        <span class="review-subtitle">{{ $t('stageReview.customVariables') }}</span>

        <bk-table
            :data="copyParams"
            :row-class-name="tableRowClassName"
        >
            <bk-table-column
                :label="$t('stageReview.alias')"
                show-overflow-tooltip
            >
                <template slot-scope="props">
                    {{ props.row.chineseName || '--' }}<i
                        v-bk-tooltips="{ content: props.row.desc }"
                        v-if="props.row.desc"
                        class="bk-icon icon-info ml5"
                    ></i>
                    <div
                        class="overlay"
                        v-if="props.row.isParamRedundant"
                    >
                        <bk-popover
                            :content="getConflictTip(props.row)"
                        >
                            <p style="width: 810px; visibility: hidden;">{{ props.row.key }}</p>
                        </bk-popover>
                    </div>
                </template>
            </bk-table-column>
            <bk-table-column
                :label="$t('stageReview.variableName')"
                prop="key"
                :formatter="nameFormatter"
                show-overflow-tooltip
            ></bk-table-column>
            <bk-table-column
                :label="$t('stageReview.variableVal')"
                prop="value"
                :formatter="valFormatter"
                show-overflow-tooltip
            ></bk-table-column>
        </bk-table>
    </section>
</template>

<script>
    import { mapState } from 'vuex'

    export default {
        props: {
            params: Array
        },

        data () {
            return {
                copyParams:  JSON.parse(JSON.stringify(this.params))
            }
        },

        computed: {
            ...mapState('atom', ['executeVariable']),
        },

        created () {
            this.copyParams = this.processReviewParams(this.params)
        },

        methods: {
            processReviewParams (params) {
                return params.map(param => ({
                    ...param,
                    isParamRedundant: this.checkParamRedundant(param)
                }))
            },

            checkParamRedundant (param) {
                return this.executeVariable.find(
                    vParam => vParam.id === param.key && vParam.readOnly === true
                )
            },

            tableRowClassName ({ row }) {
                return row.isParamRedundant ? 'redundant-row' : ''
            },

            getConflictTip (param) {
                const conflictVar = this.checkParamRedundant(param)
                if (conflictVar) {
                    return this.$t('ignoreUnchangedValues', [param.key, conflictVar.id])
                }
            },

            valFormatter (row, column, cellValue, index) {
                let res = cellValue || '--'
                if (Array.isArray(cellValue)) {
                    res = cellValue.length ? `[${cellValue.join(', ')}]` : '--'
                } else {
                    res = String(cellValue) || '--'
                }
                return res
            },

            nameFormatter (row, column, cellValue, index) {
                return (cellValue || '').replace(/^variables\./, '')
            }
        }
    }
</script>
<style lang="scss" scoped>
 ::v-deep .bk-table .redundant-row {
    color: #999;
    .cell {
        text-decoration: line-through;
    }
    
    .operation-column .cell {
        text-decoration: none;
    }
}

::v-deep .operation-column .cell {
    position: inherit;
    z-index: 2;
}

.overlay{
    position: absolute;
    left: 0;
    top: 0;
    width: 810px;
    height: 43px;
    line-height: 43px;
    background: transparent ;
    z-index: 2;
}
</style>