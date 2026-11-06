import LocalShippingIcon from "@mui/icons-material/LocalShipping";
import { Avatar } from "@mui/material";

const IconoCamion = ({ camion, color }) => {
  // 0=up, 1=right, 2=down, 3=left
  let transform = "";
  switch (camion.nodoActual.direccion) {
    case 0: // up
      transform = "rotate(-90deg)";
      break;
    case 1: // right
      transform = "rotate(0deg)";
      break;
    case 2: // down
      transform = "rotate(90deg)";
      break;
    case 3: // left
      transform = "rotate(0deg) scaleX(-1)";
      break;
    default:
      transform = "rotate(0deg)";
  }

  return (
    <Avatar
      sx={{
        bgcolor: camion.inmovilizado
          ? "red"
          : camion.saliendo || camion.despachando || camion.trasvasando
          ? "rgb(255, 178, 46)"
          : color,
        width: "90%",
        height: "90%",
        margin: "1px",
        border: `2px solid ${
          camion.inmovilizado
            ? "red"
            : camion.saliendo || camion.despachando || camion.trasvasando
            ? "rgb(255, 178, 46)"
            : color
        }`,
        transform,
        transition: "transform 0.1s",
      }}
    >
      <LocalShippingIcon sx={{ width: "75%", height: "75%" }} />
    </Avatar>
  );
};

export default IconoCamion;
