import { defineComponent } from 'vue';
import styles from './NoticeTab.module.css';

export default defineComponent({
  name: 'NoticeTab',
  
  setup() {
    return () => (
      <div class={styles.noticeTab}>
        <h2>Notice Tab Component</h2>
        <p>This is the Notice Tab component content.</p>
      </div>
    );
  }
});