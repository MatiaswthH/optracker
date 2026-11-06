// import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.tsx";
import { BrowserRouter } from "react-router";
import { ThemeProvider } from "@emotion/react";
import theme from "./theme.ts";
import "@fontsource/roboto/300.css";
import "@fontsource/roboto/400.css";
import "@fontsource/roboto/500.css";
import "@fontsource/roboto/700.css";
import "./stomp-client";
// import { SimulacionProvider } from "./context/SimulacionContext.tsx";

createRoot(document.getElementById("root")!).render(
  <BrowserRouter>
    {/* <SimulacionProvider> */}
    <ThemeProvider theme={theme}>
      <App />
    </ThemeProvider>
    {/* </SimulacionProvider> */}
  </BrowserRouter>
);
