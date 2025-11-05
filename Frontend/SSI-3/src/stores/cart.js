import { defineStore } from "pinia";

export const useCartStore = defineStore("cart", {
  state: () => ({
    carts: [],
  }),

  getters: {
    totalItems: (state) =>
      state.carts.reduce(
        (sum, cart) =>
          sum + cart.cartItems.reduce((s, i) => s + i.orderQuantity, 0),
        0
      ),

    totalPrice: (state) =>
      state.carts.reduce(
        (sum, cart) =>
          sum +
          cart.cartItems.reduce((s, i) => s + i.price * i.orderQuantity, 0),
        0
      ),
  },

  actions: {
    setCarts(newCarts) {
      this.carts = newCarts;
    },

    getCartItemBySaleItemId(saleItemId) {
      for (const cart of this.carts) {
        const item = cart.cartItems.find(
          (i) => String(i.saleItemId) === String(saleItemId)
        );
        if (item) return item;
      }
      return null;
    },

    addToCart(product, qty = 1) {
      let cart = this.carts.find((c) => c.seller?.id === product.seller?.id);

      if (!cart) {
        cart = {
          seller: product.seller || null,
          cartItems: [],
        };
        this.carts.push(cart);
      }

      const existingItem = cart.cartItems.find(
        (i) => String(i.saleItemId) === String(product.saleItemId)
      );

      if (existingItem) {
        existingItem.orderQuantity += qty;
      } else {
        cart.cartItems.push({
          ...product,
          orderQuantity: qty,
        });
      }
    },

    updateQuantity(cartId, newQty) {
      for (const cart of this.carts) {
        const item = cart.cartItems.find((i) => i.cartId === cartId);
        if (item) {
          item.orderQuantity = newQty;
          return;
        }
      }
    },

    removeItem(cartId) {
      this.carts.forEach((cart) => {
        cart.cartItems = cart.cartItems.filter((i) => i.cartId !== cartId);
      });

      this.carts = this.carts.filter((cart) => cart.cartItems.length > 0);
    },

    clearCarts() {
      this.carts = [];
    },
  },
});
