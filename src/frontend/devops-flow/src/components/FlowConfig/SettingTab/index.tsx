import { defineComponent } from 'vue';
import styles from './SettingTab.module.css';

export default defineComponent({
  name: 'SettingTab',
  
  setup() {
    return () => (
      <div class={styles.settingTab}>
        <h2>Setting Tab Component</h2>
        <p>This is the Setting Tab component content.</p>
      </div>
    );
  }
});