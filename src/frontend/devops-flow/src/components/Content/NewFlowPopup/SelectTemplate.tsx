import { defineComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { Dialog, Button, Steps, Message } from 'bkui-vue';
import styles from "./Index.module.css";

export default defineComponent({
  name: 'SelectTemplate',
  props: {
    modelValue: {
      type: Boolean
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const { t } = useI18n();
    const router = useRouter();
    const isShow = ref(false);

    return () => (
      <></>
    );
  },
});