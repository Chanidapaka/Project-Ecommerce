export async function authFetch(url, options = {}) {
  const accessToken = sessionStorage.getItem("access_token");

  const reqOptions = {
    ...options,
    headers: {
      ...(options.headers || {}),
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
    },
    ...(options.credentials ? { credentials: options.credentials } : {}),
  };

  let response = await fetch(url, reqOptions);

  if (response.status === 401 && !url.includes("/auth/refresh")) {
    try {
      const refreshRes = await fetch(
        `${import.meta.env.VITE_APP_URL_V2}/auth/refresh`,
        {
          method: "POST",
          credentials: "include",
        }
      );

      if (!refreshRes.ok) throw new Error("Refresh token failed");

      const data = await refreshRes.json();
      const newAccessToken = data.access_token;

      sessionStorage.setItem("access_token", newAccessToken);

      const retryOptions = {
        ...options,
        headers: {
          ...(options.headers || {}),
          Authorization: `Bearer ${newAccessToken}`,
        },
        ...(options.credentials ? { credentials: options.credentials } : {}),
      };

      response = await fetch(url, retryOptions);
    } catch (err) {
      handleLogout();
      throw err;
    }
  }

  return response;
}

function handleLogout() {
  sessionStorage.removeItem("access_token");
  sessionStorage.removeItem("nickname");
  sessionStorage.removeItem("user_id");
  sessionStorage.removeItem("role");
  sessionStorage.removeItem("viewMode");
  window.location.href = import.meta.env.BASE_URL + "signin";
}
