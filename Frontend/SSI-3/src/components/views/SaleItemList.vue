<script setup>
import Header from "../models/Header.vue";
import { getItems, deleteItemById } from "@/libs/FetchUtils";
import { useRouter } from "vue-router";
import { ref, onBeforeMount, computed } from "vue";
import DeleteConfirm from "../models/DeleteConfirm.vue";
import { authFetch } from "@/libs/authFetch";

const successMessage = ref("");
const errorMessage = ref("");
const showModalDelete = ref(false);
const deleteId = ref();
const isExistProduct = ref(true);
const router = useRouter();
const searchKeyWord = ref("");
const products = ref([]);

const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const pageStartNo = ref(1);
const isLastPage = ref(false);

const fetchProduct = async () => {
  sessionStorage.setItem("pageList", currentPage.value);
  sessionStorage.setItem("pageStartNoList", pageStartNo.value);
  const accessToken = sessionStorage.getItem("access_token");
  const userId = sessionStorage.getItem("user_id");

  try {
    const queryParams = new URLSearchParams();

    queryParams.set("size", pageSize.value);
    queryParams.set("page", (currentPage.value || 1) - 1);

    const url = `${
      import.meta.env.VITE_APP_URL_V2
    }/sellers/${userId}/sale-items?${queryParams.toString()}`;

    const res = await authFetch(url, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });

    const response = await res.json();

    if (!response) throw new Error(`HTTP error!`);

    if (isLastPage.value && currentPage.value !== response.totalPages) {
      isLastPage.value = false;
      currentPage.value = response.totalPages;
      pageStartNo.value = Math.max(1, response.totalPages - 10 + 1);

      return fetchProduct();
    }

    if (response.totalPages !== 0 && currentPage.value > response.totalPages) {
      currentPage.value = response.totalPages;
      pageStartNo.value = Math.max(1, response.totalPages - 10 + 1);
      return fetchProduct();
    }

    isLastPage.value = false;

    products.value = response.content || [];
    totalPages.value = response.totalPages || 1;
  } catch (e) {
    products.value = [];
  }
};

onBeforeMount(async () => {
  sessionStorage.setItem("viewMode", "list");
  currentPage.value = Number(sessionStorage.getItem("pageList")) || 1;
  pageStartNo.value = Number(sessionStorage.getItem("pageStartNoList")) || 1;

  await fetchProduct();
  successMessage.value = sessionStorage.getItem("successMessage") || "";
  errorMessage.value = sessionStorage.getItem("errorMessage") || "";
  sessionStorage.removeItem("successMessage");
  sessionStorage.removeItem("errorMessage");

  setTimeout(() => {
    successMessage.value = "";
    errorMessage.value = "";
  }, 5000);
});

const onSearch = async () => {
  currentPage.value = 1;
  await fetchProduct();
};

const clearSearch = () => {
  searchKeyWord.value = "";
  fetchProduct();
};

const pageNumber = computed(() => {
  const pages = [];
  const count = Math.min(totalPages.value, 10);
  for (let i = 0; i < count; i++) {
    pages.push(pageStartNo.value + i);
  }
  return pages;
});

async function goToPage(page) {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  if (page === 1) pageStartNo.value = 1;
  await fetchProduct();
}

async function prevPage() {
  if (currentPage.value <= 1) return;
  if (currentPage.value === pageNumber.value[0]) pageStartNo.value--;
  currentPage.value--;
  await fetchProduct();
}

async function nextPage() {
  if (currentPage.value >= totalPages.value) return;
  if (currentPage.value === pageNumber.value[pageNumber.value.length - 1])
    pageStartNo.value++;
  currentPage.value++;
  await fetchProduct();
}

async function goToLastPage() {
  pageStartNo.value = Math.max(1, totalPages.value - 10 + 1);
  currentPage.value = totalPages.value;
  await fetchProduct();
}

const handleDelete = async () => {
  const res = await deleteItemById(
    `${import.meta.env.VITE_APP_URL}/sale-items`,
    deleteId.value
  );

  if (res === 204) {
    successMessage.value = "The sale item has been deleted.";
    sessionStorage.setItem("successMessage", "The sale item has been deleted.");
  } else {
    isExistProduct.value = false;
  }

  await fetchProduct();
  showModalDelete.value = false;
  deleteId.value = null;
  window.scrollTo(0, 0);

  setTimeout(() => {
    successMessage.value = "";
  }, 5000);
};

const close = () => {
  isExistProduct.value = true;
};

const confirmDelete = (id) => {
  showModalDelete.value = true;
  deleteId.value = id;
};
</script>

<template>
  <div class="bg-[#F5F9FF] min-h-screen flex flex-col text-style">
    <Header />

    <div
      v-if="errorMessage || successMessage"
      :class="[
        'flex items-start gap-2 rounded-lg px-4 py-3 mt-4 w-full max-w-md shadow',
        errorMessage
          ? 'bg-red-100 text-red-700 border border-red-300'
          : 'bg-green-100 text-green-700 border border-green-300',
      ]"
    >
      <strong class="font-bold">{{
        errorMessage ? "Error:" : "Success:"
      }}</strong>
      <span class="text-md itbms-message">{{
        errorMessage || successMessage
      }}</span>
    </div>

    <div
      class="px-4 md:px-6 py-4 flex flex-wrap justify-end items-center gap-4"
    >   

      <div>
        <button
          @click="router.push('/sale-items/add')"
          class="itbms-sale-item-add bg-blue-400 hover:bg-blue-800 text-white px-4 py-2 rounded shadow mr-3"
        >
          Add Sale Item
        </button>
        <button
          @click="router.push('/brands')"
          class="bg-blue-400 hover:bg-blue-800 text-white px-4 py-2 rounded shadow"
        >
          Manage Brand
        </button>
      </div>
    </div>

    <div class="flex-1 px-6 pb-6">
      <div v-if="products.length === 0" class="text-center text-gray-500 mt-10">
        No sale item
      </div>

      <div v-else class="space-y-4 mt-4">
        <table
          class="w-full max-w-8/9 mx-auto bg-white shadow-md rounded-lg overflow-hidden text-base border-collapse"
        >
          <thead class="bg-[#E3ECFB] text-gray-700">
            <tr class="divide-x divide-gray-300">
              <th class="px-4 py-4 text-center">ID</th>
              <th class="px-4 py-4 text-center">Brand</th>
              <th class="px-4 py-4 text-center">Model</th>
              <th class="px-4 py-4 text-center">RAM</th>
              <th class="px-4 py-4 text-center">Storage</th>
              <th class="px-4 py-4 text-center">Color</th>
              <th class="px-4 py-4 text-center">Price</th>
              <th class="px-4 py-4 text-center">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="product in products"
              :key="product.id"
              class="itbms-row hover:bg-gray-50 divide-x divide-gray-300 text-black"
            >
              <td class="px-4 py-6 text-center itbms-id">
                {{ product.id }}
              </td>
              <td
                class="px-4 py-6 text-center font-medium text-[#0056B3] itbms-brand"
              >
                {{ product.brandName }}
              </td>
              <td class="px-4 py-6 text-center text-gray-700 itbms-model">
                {{ product.model }}
              </td>
              <td class="px-4 py-6 text-center text-gray-700 itbms-ramGb">
                {{ product.ramGb ?? "-" }}
              </td>
              <td class="px-4 py-6 text-center text-gray-700 itbms-storageGb">
                {{ product.storageGb ?? "-" }}
              </td>
              <td class="px-4 py-6 text-center text-gray-700 itbms-color">
                {{ product.color ?? "-" }}
              </td>
              <td class="px-4 py-6 text-center text-gray-700 itbms-price">
                {{ product.price.toLocaleString() }}
              </td>
              <td class="px-4 py-6 text-center space-x-2">
                <button
                  @click="router.push(`/sale-items/${product.id}/edit`)"
                  class="itbms-edit-button px-3 py-2 text-sm bg-blue-400 hover:bg-blue-500 text-white rounded font-medium"
                >
                  Edit
                </button>
                <button
                  @click="
                    () => {
                      showModalDelete = true;
                      deleteId = product.id;
                    }
                  "
                  class="itbms-delete-button px-3 py-2 text-sm bg-gray-400 hover:bg-red-400 text-white rounded font-medium"
                >
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        v-show="totalPages > 1"
        class="flex gap-3.5 items-center mt-10 justify-center"
      >
        <button
          @click="goToPage(1)"
          :disabled="currentPage === 1"
          class="px-2.5 py-1.5 rounded shadow border text-white bg-blue-500 disabled:bg-gray-300"
        >
          First
        </button>
        <button
          @click="prevPage"
          :disabled="currentPage === 1"
          class="px-2.5 py-1.5 rounded shadow border text-white bg-blue-500 disabled:bg-gray-300"
        >
          Prev
        </button>
        <button
          v-for="page in pageNumber"
          :key="page"
          @click="goToPage(page)"
          :class="[
            'px-3 py-1 rounded shadow border text-blue-500 font-bold',
            currentPage === page ? 'bg-blue-500 text-white' : '',
            `itbms-page-${page - 1}`,
          ]"
        >
          {{ page }}
        </button>
        <button
          @click="nextPage"
          :disabled="currentPage === totalPages"
          class="px-2.5 py-1.5 rounded shadow border text-white bg-blue-500 disabled:bg-gray-300"
        >
          Next
        </button>
        <button
          @click="goToLastPage"
          :disabled="currentPage === totalPages"
          class="px-2.5 py-1.5 rounded shadow border text-white bg-blue-500 disabled:bg-gray-300"
        >
          Last
        </button>
      </div>
    </div>

    <DeleteConfirm
      v-if="showModalDelete"
      :isExistProduct="isExistProduct"
      :onClose="close"
      @confirmDelete="handleDelete"
    />
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");
.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
