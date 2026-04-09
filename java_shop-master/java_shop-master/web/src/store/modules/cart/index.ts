import { defineStore } from 'pinia';
import { cartCountApi } from '/@/api/cart';
import { useUserStore } from '/@/store/modules/user';

export const useCartStore = defineStore('cart', {
  state: () => ({
    itemTotal: 0,
  }),
  actions: {
    async refreshCount() {
      const userStore = useUserStore();
      const uid = userStore.user_id;
      if (!uid) {
        this.itemTotal = 0;
        return;
      }
      try {
        const res: any = await cartCountApi({ userId: uid });
        this.itemTotal = Number(res.data) || 0;
      } catch {
        this.itemTotal = 0;
      }
    },
  },
});
