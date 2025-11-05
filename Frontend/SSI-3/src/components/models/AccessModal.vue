<script setup>
import { useRouter, useRoute } from "vue-router";
import {
  showAccessModal,
  accessMessage,
  closeAccessModal,
} from "@/libs/useAccessModal.js";

const router = useRouter();
const route = useRoute();
</script>

<template>
  <div
    class="fixed inset-0 backdrop-blur-sm bg-white/10 flex items-center justify-center z-50"
    v-if="showAccessModal"
  >
    <div class="bg-white rounded-lg shadow-lg w-full max-w-md p-6">
      <h2 class="text-xl font-bold mb-4 text-black">Access Restricted</h2>
      <p class="itbms-message text-gray-700 mb-6">{{ accessMessage }}</p>
      <div class="flex justify-end">
        <button
          @click="closeAccessModal(router, route.fullPath)"
          class="itbms-cancel-button bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition"
        >
          OK
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");
.itbms-message,
h2,
p {
  font-family: "Fredoka", sans-serif;
}
</style>
