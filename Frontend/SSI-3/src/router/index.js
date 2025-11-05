import { createWebHistory, createRouter } from "vue-router";
import HomePage from "@/components/views/HomePage.vue";
import SaleItemList from "@/components/views/SaleItemList.vue";
import SaleItemGallery from "@/components/views/SaleItemGallery.vue";
import DetailPage from "@/components/views/DetailPage.vue";
import AddSaleItemPage from "@/components/views/AddSaleItemPage.vue";
import BrandListPage from "@/components/views/BrandListPage.vue";
import AddBrandPage from "@/components/views/AddBrandPage.vue";
import RegisterPage from "@/components/views/RegisterPage.vue";
import VerifyEmail from "@/components/views/VerifyEmail.vue";
import SignInPage from "@/components/views/SignInPage.vue";
import ProfilePage from "@/components/views/ProfilePage.vue";
import EditProfilePage from "@/components/views/EditProfilePage.vue";
import CartPage from "@/components/views/CartPage.vue";
import { openAccessModal } from "../libs/useAccessModal.js";
import YourOrders from "@/components/views/YourOrders.vue";
import YourOrderDetails from "@/components/views/YourOrderDetails.vue";
import SaleOrders from "@/components/views/SaleOrders.vue";
import SaleOrderDetails from "@/components/views/SaleOrderDetails.vue";
import ResetPassword from "@/components/views/ResetPassword.vue";

const history = createWebHistory("ssi3");

const routes = [
  { path: "/", name: "HomePage", component: HomePage },
  { path: "/sale-items", name: "Sale-Items", component: SaleItemGallery },
  {
    path: "/sale-items/list",
    name: "Sale-Items-List",
    component: SaleItemList,
  },
  { path: "/sale-items/:id", name: "Detail", component: DetailPage },
  { path: "/sale-items/add", name: "AddSaleItem", component: AddSaleItemPage },
  {
    path: "/sale-items/:id/edit",
    name: "EditSaleItem",
    component: AddSaleItemPage,
  },
  { path: "/brands", name: "Brands", component: BrandListPage },
  { path: "/brands/add", name: "AddBrands", component: AddBrandPage },
  { path: "/brands/:id/edit", name: "EditBrands", component: AddBrandPage },
  { path: "/register", name: "Register", component: RegisterPage },
  { path: "/verify-email", name: "Verify-email", component: VerifyEmail },
  { path: "/signin", name: "Sign-In", component: SignInPage },
  { path: "/authentications", name: "Authentications", component: SignInPage },
  { path: "/profile", name: "Profile", component: ProfilePage },
  {
    path: "/profile/:id/edit",
    name: "EditProfile",
    component: EditProfilePage,
  },
  { path: "/cart", name: "CartPage", component: CartPage },
  { path: "/your-orders", name: "YourOrders", component: YourOrders },
  {
    path: "/your-orders/:orderId",
    name: "YourOrderDetail",
    component: YourOrderDetails,
  },
  { path: "/sale-orders", name: "SaleOrders", component: SaleOrders },
  {
    path: "/sale-orders/:orderId",
    name: "SaleOrderDetail",
    component: SaleOrderDetails,
  },
  { path: "/reset-password", name: "reset-password", component: ResetPassword },
];

const router = createRouter({ routes, history });

router.beforeEach(async (to, from, next) => {
  if (!sessionStorage.getItem("access_token")) {
    try {
      const response = await fetch(
        `${import.meta.env.VITE_APP_URL_V2}/auth/refresh`,
        {
          method: "POST",
          credentials: "include",
        }
      );

      if (response.ok) {
        const data = await response.json();
        const accessToken = data.access_token;
        sessionStorage.setItem("access_token", accessToken);

        const payload = JSON.parse(atob(accessToken.split(".")[1]));
        sessionStorage.setItem("nickname", payload.nickname);
        sessionStorage.setItem("user_id", payload.id);
        sessionStorage.setItem("role", payload.authorities[0]?.role);
      }
    } catch (err) {
      window.location.href = import.meta.env.BASE_URL + "signin";
      return;
    }
  }

  const role = sessionStorage.getItem("role");
  const userId = sessionStorage.getItem("user_id");

  const sellerOnlyPages = [
    "/sale-items/list",
    "/sale-items/add",
    "/brands",
    "/brands/add",
  ];

  const isEditPage = to.path.includes("/edit");
  const isProfileEdit =
    to.path.startsWith("/profile/") && to.path.endsWith("/edit");
  const isSellerOnly =
    sellerOnlyPages.includes(to.path) || (isEditPage && !isProfileEdit);

  if (!role && isSellerOnly) {
    openAccessModal("Please sign in to continue.", from.fullPath, "/signin");
    return next(false);
  }

  if (role === "buyer" && isSellerOnly) {
    openAccessModal(
      "This page is available to sellers only.",
      from.fullPath,
      "/sale-items"
    );
    return next(false);
  }

  if (isSellerOnly && role === "seller" && !userId) {
    openAccessModal("Access restricted to sellers only.", from.fullPath, "/");
    return next(false);
  }

  next();
});

export default router;
