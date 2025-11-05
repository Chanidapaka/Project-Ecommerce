<script setup>
const props = defineProps({
  show: Boolean,
  message: { type: String, default: "Do you want to delete this item?" },
});

const emit = defineEmits(["cancel", "confirm"]);
</script>

<template>
  <div
    class="fixed inset-0 backdrop-blur-sm bg-white/10 flex items-center justify-center z-50"
    v-if="show"
  >
    <div class="bg-white rounded-lg shadow-lg w-full max-w-md p-6">
      <h2 class="text-xl font-bold mb-4 text-black">Delete Confirmation</h2>
      <p class="itbms-message text-gray-700 mb-6">
        {{ message }}
      </p>
      <div class="flex justify-end space-x-4">
        <button
          @click="emit('cancel')"
          class="itbms-cancel-button bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition"
        >
          Cancel
        </button>
        <button
          @click="emit('confirm')"
          class="itbms-confirm-button bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 transition"
        >
          Confirm
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
