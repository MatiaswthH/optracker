import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import WarehouseIcon from "@mui/icons-material/Warehouse";
import TakeoutDiningIcon from "@mui/icons-material/TakeoutDining";
import ClearIcon from "@mui/icons-material/Clear";
import { Avatar } from "@mui/material";

type IconoMapaProps = {
  icono: "camion" | "pedido" | "almacen" | "bloqueo";
};

const IconoMapa = ({ icono }: IconoMapaProps) => {
  return (
    <>
      <Avatar
        // variant="square"
        sx={{
          bgcolor:
            icono === "camion"
              ? "green"
              : icono === "pedido"
              ? "blue"
              : icono === "almacen"
              ? "black"
              : "transparent",
          width: "90%",
          height: "90%",
          margin: "1px",
        }}
      >
        {icono === "camion" ? (
          <LocalShippingIcon sx={{ width: "75%", height: "75%" }} />
        ) : icono === "pedido" ? (
          <TakeoutDiningIcon sx={{ width: "75%", height: "85%" }} />
        ) : icono === "almacen" ? (
          <WarehouseIcon sx={{ width: "85%", height: "85%" }} />
        ) : icono === "bloqueo" ? (
          <ClearIcon
            sx={{ width: "100%", height: "100%", color: "red" }}
          ></ClearIcon>
        ) : (
          ""
        )}
      </Avatar>
    </>
  );
};

export default IconoMapa;
