import client from "@/utils/api";

export const LoginFromCustomer = (authenticationRequest) => {
 return client.post('/login-form-for-customer',authenticationRequest);
}


const API_BASE_URL =  'http://localhost:8080';

export const loginWithGoogleByRole = (role) => {
  if (!role) {
    console.error("Role is required to log in.");
    return;
  }
  const redirectUrl = `${API_BASE_URL}/api/auth/login?role=${role.toUpperCase()}`;
  window.location.href = redirectUrl;
};