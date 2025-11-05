<script setup>
import Header from "../models/Header.vue";
import { getItems } from "@/libs/FetchUtils";
import { authFetch } from "@/libs/authFetch";
import { useCartStore } from "@/stores/cart";
import { useRouter, useRoute } from "vue-router";
import {
  ref,
  computed,
  onBeforeMount,
  onBeforeUnmount,
  watch,
  onMounted,
} from "vue";

const cartStore = useCartStore();

const router = useRouter();
const route = useRoute();
const isInitialized = ref(false);

const products = ref([]);
const availableBrands = ref([]);
const storageOptions = ref([]);

const showBrandDropdown = ref(false);
const showPriceDropdown = ref(false);
const showStorageDropdown = ref(false);

const sortSaleItemDirection = ref("none");
const selectedBrands = ref([]);
const selectedPrice = ref("");
const selectedStorages = ref([]);
const searchKeyWord = ref("");

const priceOptions = [
  { label: "0 - 5,000 Baht", min: 0, max: 5000 },
  { label: "5,001 - 10,000 Baht", min: 5001, max: 10000 },
  { label: "10,001 - 20,000 Baht", min: 10001, max: 20000 },
  { label: "20,001 - 30,000 Baht", min: 20001, max: 30000 },
  { label: "30,001 - 40,000 Baht", min: 30001, max: 40000 },
  { label: "40,001 - 50,000 Baht", min: 40001, max: 50000 },
];
const filterPriceLower = ref("");
const filterPriceUpper = ref("");

const showToast = ref(false);
const toastMessage = ref("");
const toastType = ref("success");

const fetchStorages = async () => {
  try {
    const response = await getItems(
      `${import.meta.env.VITE_APP_URL_V2}/sale-items/storages`
    );
    if (response?.status === 400) {
      storageOptions.value = [];
    } else {
      storageOptions.value = response ?? [];
    }
  } catch (e) {
    storageOptions.value = [];
  }
};

const currentPage = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const pageStartNo = ref(1);
const isLastPage = ref(false);

const filterWrapper = ref(null);

const handleClickOutside = (event) => {
  if (filterWrapper.value && !filterWrapper.value.contains(event.target)) {
    showBrandDropdown.value = false;
    showPriceDropdown.value = false;
    showStorageDropdown.value = false;
  }
};

onMounted(() => {
  window.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  window.removeEventListener("click", handleClickOutside);
});

onBeforeMount(async () => {
  sessionStorage.setItem("viewMode", "gallery");
  selectedBrands.value = JSON.parse(
    sessionStorage.getItem("selectedBrands") || "[]"
  );
  selectedPrice.value = sessionStorage.getItem("selectedPrice") || "";
  selectedStorages.value = JSON.parse(
    sessionStorage.getItem("selectedStorages") || "[]"
  );
  sortSaleItemDirection.value =
    sessionStorage.getItem("sortSaleItemDirection") || "none";
  pageSize.value = Number(sessionStorage.getItem("size")) || 10;

  searchKeyWord.value = sessionStorage.getItem("searchKeyWord") || "";

  const prevPath = sessionStorage.getItem("prevPath") || "";
  const isFromDetailPage = /^\/sale-items\/\d+$/.test(prevPath);

  if (prevPath === "/sale-items" || isFromDetailPage) {
    currentPage.value = Number(sessionStorage.getItem("page")) || 1;
    pageStartNo.value = Number(sessionStorage.getItem("pageStartNo")) || 1;
  } else {
    currentPage.value = 1;
    pageStartNo.value = 1;
    sessionStorage.setItem("page", 1);
    sessionStorage.setItem("pageStartNo", 1);
  }

  await fetchBrands();
  await fetchProduct();

  isInitialized.value = true;
  sessionStorage.setItem("prevPath", route.path);

  successMessage.value = sessionStorage.getItem("successMessage") || "";
  errorMessage.value = sessionStorage.getItem("errorMessage") || "";
  sessionStorage.removeItem("successMessage");
  sessionStorage.removeItem("errorMessage");

  setTimeout(() => {
    successMessage.value = "";
    errorMessage.value = "";
  }, 5000);
});

onBeforeUnmount(() => {
  sessionStorage.setItem("prevPath", "");
});

onMounted(() => {
  fetchStorages();
});

const fetchProduct = async () => {
  sessionStorage.setItem("page", currentPage.value);
  sessionStorage.setItem("pageStartNo", pageStartNo.value);
  sessionStorage.setItem("selectedPrice", selectedPrice.value);
  sessionStorage.setItem("size", pageSize.value);
  sessionStorage.setItem("sortSaleItemDirection", sortSaleItemDirection.value);
  sessionStorage.setItem(
    "selectedBrands",
    JSON.stringify(selectedBrands.value)
  );
  sessionStorage.setItem(
    "selectedStorages",
    JSON.stringify(selectedStorages.value)
  );

  sessionStorage.setItem("searchKeyWord", searchKeyWord.value);

  try {
    const queryParams = new URLSearchParams();

    if (sortSaleItemDirection.value && sortSaleItemDirection.value !== "none") {
      queryParams.set("sortDirection", sortSaleItemDirection.value);
      queryParams.set("sortField", "brand.name");
    } else {
      queryParams.set("sortDirection", "asc");
      queryParams.set("sortField", "createdOn");
    }

    selectedBrands.value.forEach((brand) => {
      const trimmed = brand?.trim();
      if (trimmed) queryParams.append("filterBrands", trimmed);
    });

    if (selectedPrice.value && selectedPrice.value !== "custom") {
      const price = priceOptions.find((p) => p.label === selectedPrice.value);
      if (price) {
        queryParams.set("filterPriceLower", price.min);
        queryParams.set("filterPriceUpper", price.max);
      }
    } else if (selectedPrice.value === "custom") {
      if (filterPriceLower.value)
        queryParams.set("filterPriceLower", Number(filterPriceLower.value));

      if (filterPriceUpper.value) {
        queryParams.set("filterPriceUpper", Number(filterPriceUpper.value));
      } else if (filterPriceLower.value) {
        queryParams.set("filterPriceUpper", Number(filterPriceLower.value));
      }
    }

    selectedStorages.value.forEach((storage) => {
      queryParams.append(
        "filterStorages",
        storage === "" || storage === null ? "" : storage
      );
    });

    if (searchKeyWord.value && searchKeyWord.value.trim()) {
      queryParams.set("searchKeyWord", searchKeyWord.value.trim());
    }

    queryParams.set("size", pageSize.value);
    queryParams.set("page", (currentPage.value || 1) - 1);

    const url = `${
      import.meta.env.VITE_APP_URL_V2
    }/sale-items?${queryParams.toString()}`;
    const response = await getItems(url);

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

const fetchBrands = async () => {
  try {
    const response = await getItems(`${import.meta.env.VITE_APP_URL}/brands`);
    availableBrands.value = response
      .map((b) => b.name)
      .sort((a, b) => a.localeCompare(b));
  } catch (e) {
    availableBrands.value = [];
  }
};

watch(
  [
    sortSaleItemDirection,
    selectedBrands,
    pageSize,
    selectedPrice,
    selectedStorages,
  ],
  async () => {
    if (!isInitialized.value) return;
    currentPage.value = 1;
    pageStartNo.value = 1;
    await fetchProduct();
  }
);

watch(selectedPrice, async (newVal) => {
  if (newVal) {
    if (
      newVal === "custom" &&
      !filterPriceLower.value &&
      !filterPriceUpper.value
    )
      return;
    await fetchProduct();
    showPriceDropdown.value = false;
  }
});

const toggleDropdown = (name) => {
  showBrandDropdown.value = name === "brand" ? !showBrandDropdown.value : false;
  showPriceDropdown.value = name === "price" ? !showPriceDropdown.value : false;
  showStorageDropdown.value =
    name === "storage" ? !showStorageDropdown.value : false;
};

const removeSelectedBrand = (brand) => {
  selectedBrands.value = selectedBrands.value.filter((b) => b !== brand);
};

const clearPriceFilter = async () => {
  selectedPrice.value = "";
  filterPriceLower.value = "";
  filterPriceUpper.value = "";
  await fetchProduct();
};

const setSort = (direction) => {
  sortSaleItemDirection.value = direction;
};

const clearAllFilters = () => {
  selectedBrands.value = [];
  selectedPrice.value = "";
  filterPriceLower.value = "";
  filterPriceUpper.value = "";
  selectedStorages.value = [];
  fetchProduct();
};

const onSearch = async () => {
  currentPage.value = 1;
  await fetchProduct();
};

const clearSearch = () => {
  searchKeyWord.value = "";
  fetchProduct();
};

const applyCustomPrice = async () => {
  if (selectedPrice.value === "custom") {
    await fetchProduct();
    showPriceDropdown.value = false;
  }
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
  if (page === 1) {
    pageStartNo.value = 1;
  }

  await fetchProduct();
}

async function prevPage() {
  if (currentPage.value <= 1) return;

  if (currentPage.value === pageNumber.value[0]) {
    pageStartNo.value--;
  }

  currentPage.value--;

  await fetchProduct();
}

async function nextPage() {
  if (currentPage.value >= totalPages.value) return;

  if (currentPage.value === pageNumber.value[pageNumber.value.length - 1]) {
    pageStartNo.value++;
  }

  currentPage.value++;

  await fetchProduct();
}

async function goToLastPage() {
  isLastPage.value = true;
  pageStartNo.value = Math.max(1, totalPages.value - 10 + 1);
  currentPage.value = totalPages.value;

  await fetchProduct();
}

onMounted(() => {
  const { verifyStatus, message } = route.query;
  if (verifyStatus && message) {
    toastMessage.value = message;
    toastType.value = verifyStatus;
    showToast.value = true;

    setTimeout(() => {
      showToast.value = false;
      router.replace({ query: {} });
    }, 5000);
  }
});

const successMessage = ref("");
const errorMessage = ref("");

const addToCart = async (product) => {
  try {
    const accessToken = sessionStorage.getItem("access_token");
    const userId = sessionStorage.getItem("user_id");

    if (!accessToken || !userId) {
      errorMessage.value = "⚠️ Please login first!";
      router.push("/signin");
      return;
    }

    const res = await authFetch(
      `${import.meta.env.VITE_APP_URL_V2}/cart/${userId}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify({
          saleItemId: product.id,
          quantity: 1,
        }),
      }
    );

    if (!res.ok) {
      const errData = await res.json().catch(() => ({}));
      throw new Error(errData.message || "Failed to add item to cart");
    }

    const data = await res.json();

    cartStore.addToCart({
      cartId: data.id,
      saleItemId: product.id,
      description: product.model,
      price: product.price,
      quantity: 1,
      sellerId: product.sellerId || 0,
      sellerName: product.sellerName || "Unknown",
      image: product.image || "/images/NoImage.jpg",
    });

    successMessage.value = `✅ Added "${product.model}" to cart!`;
    errorMessage.value = "";
    setTimeout(() => {
      successMessage.value = "";
      errorMessage.value = "";
    }, 5000);
  } catch (err) {
    console.error(err);
    errorMessage.value = `❌ ${err.message}`;
    successMessage.value = "";
    setTimeout(() => {
      successMessage.value = "";
      errorMessage.value = "";
    }, 5000);
  }
};
</script>

<template>
  <div class="bg-[#F5F9FF] min-h-screen flex flex-col text-style relative">
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
      class="flex flex-col md:flex-row md:justify-between md:items-center w-full px-4 md:px-6 mt-4 gap-4"
      ref="filterWrapper"
    >
      <div class="flex flex-col gap-3 px-0.5 py-1">
        <div class="flex flex-wrap items-start gap-2">
          <div class="relative w-full md:w-64">
            <div
              class="itbms-brand-filter border border-[#C7D7F5] px-5 py-2 text-sm rounded cursor-pointer bg-white min-h-[40px]"
              @click="toggleDropdown('brand')"
            >
              <template v-if="selectedBrands.length > 0">
                <span class="font-semibold text-gray-800 mr-2">Brand:</span>
                <div class="grid grid-cols-2 gap-3 max-w-full">
                  <span
                    v-for="brand in selectedBrands"
                    :key="brand"
                    class="itbms-filter-item bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs flex items-center gap-1"
                  >
                    {{ brand }}
                    <button
                      @click.stop="removeSelectedBrand(brand)"
                      class="text-blue-800 hover:text-red-500 font-bold text-xs"
                    >
                      ×
                    </button>
                  </span>
                </div>
              </template>
              <template v-else>
                <div class="flex flex-col gap-0.5">
                  <p class="text-gray-800 font-semibold m-0">Brand</p>
                  <p class="text-gray-400 text-sm m-0">Filter by brand(s)</p>
                </div>
              </template>
            </div>
            <div
              v-if="showBrandDropdown"
              class="absolute z-10 mt-1 bg-white border border-gray-300 rounded shadow px-3 py-3 w-fit grid grid-rows-2 grid-flow-col gap-x-6 gap-y-2"
              @click.stop
            >
              <label
                v-for="brand in availableBrands"
                :key="brand"
                class="flex items-center gap-1 text-sm text-gray-700"
              >
                <input
                  type="checkbox"
                  :value="brand"
                  v-model="selectedBrands"
                  class="accent-blue-500"
                />
                {{ brand }}
              </label>
            </div>
          </div>

          <div class="relative w-full md:w-64">
            <div
              class="itbms-price-filter border border-[#C7D7F5] px-5 py-2 text-sm rounded cursor-pointer bg-white min-h-[40px]"
              @click="toggleDropdown('price')"
            >
              <template
                v-if="selectedPrice || filterPriceLower || filterPriceUpper"
              >
                <span class="font-semibold text-gray-800 mr-2">Price:</span>
                <span
                  class="itbms-filter-item bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs flex items-center gap-1"
                >
                  {{
                    selectedPrice === "custom"
                      ? `Custom: ${filterPriceLower || "-"} - ${
                          filterPriceUpper || "-"
                        }`
                      : selectedPrice
                  }}
                  <button
                    @click.stop="clearPriceFilter"
                    class="text-blue-800 hover:text-red-500 font-bold text-xs"
                  >
                    ×
                  </button>
                </span>
              </template>
              <template v-else>
                <div class="flex flex-col gap-0.5">
                  <p class="text-gray-800 font-semibold m-0">Price</p>
                  <p class="text-gray-400 text-sm m-0">Filter by price range</p>
                </div>
              </template>
            </div>
            <div
              v-if="showPriceDropdown"
              class="absolute z-10 mt-1 bg-white border border-gray-300 rounded shadow px-3 py-3 w-fit grid gap-y-2"
              @click.stop
            >
              <label
                v-for="price in priceOptions"
                :key="price.label"
                class="flex items-center gap-1 text-sm text-gray-700"
              >
                <input
                  type="radio"
                  name="priceRange"
                  :value="price.label"
                  v-model="selectedPrice"
                  class="accent-blue-500"
                />
                {{ price.label }}
              </label>

              <label class="flex items-center gap-1 text-sm text-gray-700 mt-1">
                <input
                  type="radio"
                  name="priceRange"
                  value="custom"
                  v-model="selectedPrice"
                  class="accent-blue-500"
                />
                Custom:
              </label>
              <div
                class="flex gap-2 ml-6 mt-1"
                v-if="selectedPrice === 'custom'"
              >
                <input
                  type="number"
                  v-model="filterPriceLower"
                  placeholder="Min"
                  class="border p-1 rounded w-20"
                />
                <input
                  type="number"
                  v-model="filterPriceUpper"
                  placeholder="Max"
                  class="border p-1 rounded w-20"
                />
              </div>

              <button
                @click="applyCustomPrice"
                class="bg-blue-500 text-white text-sm py-1 rounded hover:bg-blue-600 mt-2"
              >
                Apply
              </button>
            </div>
          </div>

          <div class="relative w-full md:w-64">
            <div
              class="itbms-storage-filter border border-[#C7D7F5] px-5 py-2 text-sm rounded cursor-pointer bg-white min-h-[40px]"
              @click="toggleDropdown('storage')"
            >
              <template v-if="selectedStorages.length > 0">
                <span class="font-semibold text-gray-800 mr-2">Storage:</span>
                <div class="grid grid-cols-2 gap-3 max-w-full">
                  <span
                    v-for="storage in selectedStorages"
                    :key="storage"
                    class="itbms-filter-item bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs flex items-center gap-1"
                  >
                    {{
                      storage === "" || storage === null
                        ? "Not specified"
                        : storage
                    }}
                    <button
                      @click.stop="
                        selectedStorages = selectedStorages.filter(
                          (s) => s !== storage
                        )
                      "
                      class="text-blue-800 hover:text-red-500 font-bold text-xs"
                    >
                      ×
                    </button>
                  </span>
                </div>
              </template>
              <template v-else>
                <div class="flex flex-col gap-0.5">
                  <p class="text-gray-800 font-semibold m-0">Storage</p>
                  <p class="text-gray-400 text-sm m-0">
                    Filter by storage size
                  </p>
                </div>
              </template>
            </div>
            <div
              v-if="showStorageDropdown"
              class="absolute z-10 mt-1 bg-white border border-gray-300 rounded shadow px-3 py-3 w-fit grid gap-y-2"
              @click.stop
            >
              <label
                v-for="storage in storageOptions"
                :key="storage"
                class="flex items-center gap-1 text-sm text-gray-700"
              >
                <input
                  type="checkbox"
                  :value="storage"
                  v-model="selectedStorages"
                  class="accent-blue-500"
                />
                {{
                  storage === "" || storage === null ? "Not specified" : storage
                }}
              </label>
            </div>
          </div>

          <button
            @click="clearAllFilters"
            class="bg-[#00BCE6] hover:bg-[#0056B3] text-white text-sm px-4 py-2 rounded"
          >
            Clear
          </button>
        </div>
      </div>

      <div class="flex items-center gap-4">
        <div class="flex items-center gap-2">
          <label class="text-sm text-black">Show</label>
          <select
            v-model="pageSize"
            class="border border-gray-300 rounded px-2 py-1 text-sm text-black"
          >
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select>
        </div>

        <div class="flex items-center gap-2">
          <button
            @click="setSort('none')"
            :class="[
              'px-3 py-1 rounded text-sm border',
              sortSaleItemDirection === 'none'
                ? 'bg-blue-500 text-white'
                : 'bg-white text-gray-700',
            ]"
          >
            <img
              src="/images/DefaultGallery.png"
              alt="Default"
              class="w-5 h-5"
            />
          </button>
          <button
            @click="setSort('asc')"
            :class="[
              'px-3 py-1 rounded text-sm border',
              sortSaleItemDirection === 'asc'
                ? 'bg-blue-500 text-white'
                : 'bg-white text-gray-700',
            ]"
          >
            <img
              src="/images/SortAsc.png"
              alt="Sort Ascending"
              class="w-5 h-5"
            />
          </button>
          <button
            @click="setSort('desc')"
            :class="[
              'px-3 py-1 rounded text-sm border',
              sortSaleItemDirection === 'desc'
                ? 'bg-blue-500 text-white'
                : 'bg-white text-gray-700',
            ]"
          >
            <img
              src="/images/SortDesc.png"
              alt="Sort Descending"
              class="w-5 h-5"
            />
          </button>
        </div>
      </div>
    </div>

    <div class="px-4 md:px-6 py-4">
      <div class="flex flex-wrap justify-between items-center gap-4 w-full">
        <div class="flex items-center w-full md:w-1/3 gap-2">
          <div
            class="flex items-center w-full bg-white border border-[#C7D7F5] shadow-sm focus-within:ring-2 focus-within:ring-[#0056B3] rounded"
          >
            <input
              v-model="searchKeyWord"
              type="text"
              placeholder="Search..."
              class="w-full px-3 py-2 focus:outline-none text-sm text-gray-700 placeholder-gray-400 bg-white rounded-l"
              @keyup.enter="onSearch"
            />

            <button
              @click="onSearch"
              class="px-3 py-2 bg-blue-400 hover:bg-blue-800 transition rounded-r"
            >
              <img
                src="/images/Search.png"
                alt="Search"
                class="h-5 w-5 filter brightness-0 invert"
              />
            </button>
          </div>

          <button
            v-if="searchKeyWord"
            @click="clearSearch"
            class="px-3 py-2 bg-gray-200 hover:bg-gray-300 text-gray-700 transition rounded shadow"
          >
            ×
          </button>
        </div>
      </div>
    </div>

    <div class="flex-1 px-6 pb-6">
      <div v-if="products.length === 0" class="text-center text-gray-500 mt-10">
        No sale item
      </div>
      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-6 mt-4">
        <div
          v-for="product in products"
          :key="product.id"
          class="itbms-row bg-white rounded-lg shadow hover:shadow-md transition p-6 text-center cursor-pointer"
          @click="router.push(`/sale-items/${product.id}`)"
        >
          <img
            src="/images/NoImage.jpg"
            alt="product"
            class="h-40 object-cover rounded mb-3 mx-auto"
          />
          <h3
            class="itbms-brand text-[#0056B3] font-semibold text-base truncate mb-1"
          >
            {{ product.brandName }}
          </h3>
          <p class="itbms-model text-gray-500 text-sm">{{ product.model }}</p>
          <p class="text-gray-500 text-sm">
            <span class="itbms-ramGb">{{ product.ramGb ?? "-" }}</span
            >/<span class="itbms-storageGb">{{
              product.storageGb ?? "-"
            }}</span>
            GB
          </p>

          <span
            v-for="color in Array.isArray(product.color)
              ? product.color
              : [product.color]"
            :key="color"
            class="itbms-color text-gray-500 text-sm"
          >
            {{ color ?? "-" }}
          </span>
          <p class="itbms-price text-gray-500 text-sm">
            Baht <span>{{ product.price.toLocaleString() }}</span>
          </p>

          <div class="mt-2">
            <button
              v-if="product.quantity > 0"
              @click.stop="addToCart(product)"
              class="itbms-add-to-cart-button w-full bg-blue-600 text-white font-semibold py-2 rounded-lg shadow hover:bg-blue-700 transition"
            >
              🛒 Add to Cart
            </button>
            <p v-else class="text-red-600 font-semibold text-xl mt-3">
              OUT OF STOCK !
            </p>
          </div>
        </div>
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
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Fredoka:wght@400;500&display=swap");

.text-style {
  font-family: "Fredoka", sans-serif;
}
</style>
