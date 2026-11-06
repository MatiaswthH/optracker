import WarehouseIcon from "@mui/icons-material/Warehouse";
import { Avatar } from "@mui/material";

const IconoAlmacen = ({ almacen }) => {
  return (
    <>
      <Avatar
        // variant="square"
        sx={{
          bgcolor: almacen.cargaGLP === 0 ? "red" : "black",
          width: "90%",
          height: "90%",
          margin: "1px",
        }}
      >
        <WarehouseIcon sx={{ width: "85%", height: "85%" }} />
      </Avatar>
    </>
  );
};

export default IconoAlmacen;
