import { Route, Routes } from "react-router";
import MainLayout from "./layout/MainLayout";
import PLGHome from "./pages/PLGHome";
import Simulacion from "./pages/Simulacion";
import EstadisticasSimulacion from "./pages/EstadisticasSimulacion";
import "./index.css";
import { GestionCamiones } from "./components/RegistroCamiones";
import { GestionAverias } from "./components/RegistroAverias";
import { GestionMantenimientos } from "./components/RegistroMantenimientos";
import { GestionPedidos } from "./components/RegistroPedidos";

function App() {
  return (
    <>
      <Routes>
        <Route Component={MainLayout}>
          <Route path="/" Component={PLGHome} />
          <Route path="/simulacion" Component={Simulacion} />
          <Route path="/camiones" element={<GestionCamiones />} />
          <Route path="/averias" element={<GestionAverias />} />
          <Route path="/mantenimientos" element={<GestionMantenimientos />} />
          <Route path="/pedidos" element={<GestionPedidos />} />
          <Route path="/estadisticas" element={<EstadisticasSimulacion />} />
        </Route>
      </Routes>
    </>
  );
}

export default App;
