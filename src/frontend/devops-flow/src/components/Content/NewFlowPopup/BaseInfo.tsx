import { defineComponent, ref, onMounted, onUnmounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { Select, Form, Input, Tag } from 'bkui-vue';
import { SvgIcon } from "@/components/SvgIcon";
import styles from "./Index.module.css";

export default defineComponent({
  name: 'BaseInfo',
  components: {
    SvgIcon
  },
  props: {
    modelValue: {
      type: Boolean
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const { t } = useI18n();
    const router = useRouter();
    const formRef = ref();
    const FormData = ref({
      flowName: '',
      desc: ''
    });
    const authoringEnv = ref('bike');
    const authoringEnvList = ref([
      {
        value: 'fitness',
        label: '健身',
      },
      {
        value: 'bike',
        label: '骑车',
      }
    ])
    const selectRef = ref();
    const isPopoverVisible = ref(false);
    const trigger = ref<'default' | 'manual'>('manual');

    onMounted(() => {
      document.addEventListener('click', handleClickOutside);
    });

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside);
    });

    const handleClickOutside = (event: MouseEvent) => {
      if (isPopoverVisible.value && selectRef.value) {
        const selectElement = selectRef.value.$el;
        const popoverElement = document.querySelector('.bk-select-search-wrapper');

        const isClickInsideSelect = selectElement?.contains(event.target as Node);
        const isClickInsidePopover = popoverElement?.contains(event.target as Node);

        if (!isClickInsideSelect && !isClickInsidePopover) {
          selectRef.value?.hidePopover();
        }
      }
    }

    const showPopover = (event: MouseEvent) => {
      const popoverElement = document.querySelector('.bk-select-search-wrapper');
      const isClickInsidePopover = popoverElement?.contains(event.target as Node);

      if (isPopoverVisible.value && !isClickInsidePopover) {
        selectRef.value?.hidePopover();
      } else {
        selectRef.value?.showPopover();
      }
    }

    const handlePopoverHide = (value: boolean) => {
      isPopoverVisible.value = value;
    }

    return () => (
      <Form
        class={styles.baseInfo}
        ref={formRef.value}
        model={FormData.value}
        form-type="vertical"
      >
        <div class={styles.baseItem}>
          <p class={styles.baseTitle}>{t('flow.content.basicInfo')}</p>
          <Form.FormItem
            label={t('flow.content.flowName')}
            property="flowName"
            placeholder={t('flow.content.inputFlowName')}
            required
          >
            <Input
              v-model={FormData.value.flowName}
            ></Input>
          </Form.FormItem>
          <Form.FormItem
            label={t('flow.content.description')}
            property="desc"
          >
            <Input
              v-model={FormData.value.desc}
              type="textarea"
            ></Input>
          </Form.FormItem>
        </div>
        <div class={styles.baseItem}>
          <p class={styles.baseTitle}>
            <span>{t('flow.content.creationEnvironment')}</span>
            <span class={styles.titleSet}>
              <SvgIcon
                name='jump'
                size={12}
                class={styles.jumpIcon}
              />
              {t('flow.content.environmentManagement')}
            </span>
          </p>
          <div class={styles.authoringContent}>
            <p class={styles.authoringHeader} onClick={showPopover}>
              <Select
                ref={selectRef}
                v-model={authoringEnv.value}
                filterable
                trigger={trigger.value}
                searchPlaceholder={t('flow.content.searchEnvironment')}
                popoverMinWidth={240}
                onToggle={handlePopoverHide}
              >
                {
                  authoringEnvList.value.map(i => (
                    <Select.Option
                      key={i.value}
                      id={i.value}
                      name={i.label}
                    ></Select.Option>
                  ))
                }
              </Select>
            </p>
            {
              authoringEnv.value ? (
                <div class="p-lg">
                  <div class={styles.envItem}>
                    <p class={styles.envItemTit}>
                      {t('flow.content.creationNode')}
                      <SvgIcon
                        name='set-line'
                        size={12}
                        class="cursor-pointer"
                      />
                    </p>
                    <div class={styles.nodeTag}>
                      <Tag>ins-be4830935d0ed3db</Tag>
                      <Tag>ins-be4830935d0ed3db</Tag>
                    </div>
                  </div>
                  <div class={styles.envItem}>
                    <p class={styles.envItemTit}>{t('flow.content.workspace')}</p>
                    <div>{t('flow.content.workSpaceDesc')}</div>
                  </div>
                </div>
              ) : (
                <p class={styles.noData}>{t('flow.content.previewDetailsAfterEnvironmentSelection')}</p>
              )
            }
          </div>
        </div>
      </Form>
    );
  },
});