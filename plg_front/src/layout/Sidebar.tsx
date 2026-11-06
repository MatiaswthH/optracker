import {
  Drawer,
  List,
  Divider,
  ListItemIcon,
  ListItemText,
  ListItemButton,
  Typography,
  Box,
  IconButton,
} from "@mui/material";
import { NavLink } from "react-router-dom";
import PlayCircleOutlineIcon from "@mui/icons-material/PlayCircleOutline";
import ShoppingCartIcon from "@mui/icons-material/ShoppingCart";
import LocalShippingIcon from "@mui/icons-material/LocalShipping";
// import BarChartIcon from "@mui/icons-material/BarChart";
// import PersonIcon from "@mui/icons-material/Person";
// import SettingsIcon from "@mui/icons-material/Settings";
import MenuIcon from "@mui/icons-material/Menu";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import BuildIcon from "@mui/icons-material/Build"; // Icono para Averías
import HandymanIcon from "@mui/icons-material/Handyman"; // Icono para Mantenimientos
import BarChartIcon from "@mui/icons-material/BarChart";

interface SidebarProps {
  isVisible: boolean;
  onToggle: () => void;
}

const drawerWidth = 240;

const Sidebar = ({ isVisible, onToggle }: SidebarProps) => {
  const sidebarItems = [
    {
      icon: <PlayCircleOutlineIcon />,
      text: "Simulación",
      link: "/simulacion",
    },
    { icon: <ShoppingCartIcon />, text: "Pedidos", link: "/pedidos" },
    { icon: <LocalShippingIcon />, text: "Flota", link: "/camiones" },
    { icon: <BuildIcon />, text: "Averías", link: "/averias" },
    { icon: <HandymanIcon />, text: "Mantenimientos", link: "/mantenimientos" },
    { icon: <BarChartIcon />, text: "Estadísticas", link: "/estadisticas" },
    // { icon: <BarChartIcon />, text: "Dashboard", link: "/" },
    // { icon: <PersonIcon />, text: "Clientes", link: "/" },
    // { icon: <SettingsIcon />, text: "Ajustes", link: "/" },
  ];

  const drawerContent = (
    <Box sx={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <Box sx={{ p: 2, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <Typography variant="h6" component="div" fontWeight="600" sx={{ color: "white" }}>
          PLG Logistics
        </Typography>
        <IconButton onClick={onToggle} sx={{ color: "white" }}>
          <ChevronLeftIcon />
        </IconButton>
      </Box>
      <Divider sx={{ bgcolor: "rgba(255, 255, 255, 0.12)" }} />
      <List sx={{ flexGrow: 1 }}>
        {sidebarItems.map((item, index) => (
          <ListItemButton
            component={NavLink}
            to={item.link}
            key={index}
            sx={{
              color: "white",
              "&.active": {
                backgroundColor: "secondary.main",
              },
              "&:hover": {
                backgroundColor: "rgba(255, 255, 255, 0.08)",
              },
            }}
          >
            <ListItemIcon sx={{ color: "inherit" }}>{item.icon}</ListItemIcon>
            <ListItemText primary={item.text} />
          </ListItemButton>
        ))}
      </List>
    </Box>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { sm: isVisible ? drawerWidth : 0 }, flexShrink: { sm: 0 } }}
    >
      <IconButton
        color="inherit"
        aria-label="open drawer"
        edge="start"
        onClick={onToggle}
        sx={{ mr: 2, display: { sm: "none" }, position: 'absolute', top: 16, left: 16, zIndex: 1300, color: 'primary.main' }}
      >
        <MenuIcon />
      </IconButton>
      <Drawer
        variant="temporary"
        open={isVisible}
        onClose={onToggle}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile.
        }}
        sx={{
          display: { xs: "block", sm: "none" },
          "& .MuiDrawer-paper": {
            boxSizing: "border-box",
            width: drawerWidth,
            bgcolor: "primary.main",
          },
        }}
      >
        {drawerContent}
      </Drawer>
      <Drawer
        variant="persistent"
        open={isVisible}
        sx={{
          display: { xs: "none", sm: "block" },
          "& .MuiDrawer-paper": {
            boxSizing: "border-box",
            width: drawerWidth,
            bgcolor: "primary.main",
            borderRight: 'none'
          },
        }}
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
};

export default Sidebar;
