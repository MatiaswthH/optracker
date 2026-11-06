import { Outlet } from "react-router";
import { Box, CssBaseline, Fab } from "@mui/material";
import Sidebar from "./Sidebar";
import { useState } from "react";
import MenuIcon from "@mui/icons-material/Menu";

const MainLayout = () => {
  const [sidebarVisible, setSidebarVisible] = useState(true);

  return (
    <Box sx={{ display: "flex", height: "100vh" }}>
      <CssBaseline />
      <Sidebar
        isVisible={sidebarVisible}
        onToggle={() => setSidebarVisible(!sidebarVisible)}
      />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          //p: 3,
          width: { sm: `calc(100% - 15rem)` },
          height: "100%",
          overflow: "auto",
        }}
      >
        <Outlet context={{ sidebarVisible }} />
        {!sidebarVisible && (
          <Fab
            color="primary"
            size="small"
            aria-label="Mostrar menú"
            onClick={() => setSidebarVisible(true)}
            sx={{
              position: "fixed",
              top: 5,
              left: 5,
              zIndex: 1300,
            }}
          >
            <MenuIcon />
          </Fab>
        )}
      </Box>
    </Box>
  );
};

export default MainLayout;
