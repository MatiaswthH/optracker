// import ArrowDropUpIcon from "@mui/icons-material/ArrowDropUp";
// import ArrowRightIcon from "@mui/icons-material/ArrowRight";
// import ArrowLeftIcon from "@mui/icons-material/ArrowLeft";
// import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
// import CircleIcon from "@mui/icons-material/Circle";
import { Box } from "@mui/material";
import React from "react";

const IconoRuta = React.memo(() => {
  return (
    <>
      <Box
        sx={{
          width: "100%",
          height: "100%",
          margin: "auto",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Box
          sx={{
            width: "25%",
            height: "25%",
            borderRadius: "50%",
            background: "green",
            margin: "auto",
          }}
        />
      </Box>
      {/* {nodoRuta.direccion === 0 ? (
        <ArrowDropUpIcon sx={{ width: "85%", height: "85%", color: "green" }} />
      ) : nodoRuta.direccion === 1 ? (
        <ArrowRightIcon sx={{ width: "85%", height: "85%", color: "green" }} />
      ) : nodoRuta.direccion === 2 ? (
        <ArrowDropDownIcon
          sx={{ width: "85%", height: "85%", color: "green" }}
        />
      ) : nodoRuta.direccion === 3 ? (
        <ArrowLeftIcon sx={{ width: "85%", height: "85%", color: "green" }} />
      ) : (
        ""
      )} */}
    </>
  );
});

export default IconoRuta;
