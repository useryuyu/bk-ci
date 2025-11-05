import { defineComponent } from 'vue';
import styles from './TriggerTab.module.css';

export default defineComponent({
  name: 'TriggerTab',
  
  setup() {
    return () => (
      <div class={styles.triggerTab}>
        <h2>Trigger Tab Component</h2>
        <p>This is the Trigger Tab component content.</p>
      </div>
    );
  }
});