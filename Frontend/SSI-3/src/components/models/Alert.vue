<script setup></script>

<template>
  <div class="fixed inset-0 bg-black/50 flex justify-center items-start pt-20">
    <div
      class="bg-white rounded-xl shadow-lg p-6 w-full max-w-md -translate-y-15"
    >
      <slot name="body">default message</slot>
      <div class="flex justify-center">
        <slot name="button">
          <button
            class="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded"
          >
            Close
          </button>
        </slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");

* {
  font-family: "Fredoka", sans-serif;
}
</style>
