import { ref } from "vue";

export const showAccessModal = ref(false);
export const accessMessage = ref("");
let returnPath = "/";

export function openAccessModal(msg, prevPath = "/", redirectTo = null) {
  accessMessage.value = msg;
  returnPath = redirectTo || prevPath;
  showAccessModal.value = true;
}

export function closeAccessModal(router) {
  showAccessModal.value = false;

  if (returnPath) {
    router.push(returnPath);
  } else {
    router.push("/");
  }
}
