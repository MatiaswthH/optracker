import { Box, Stack, Typography } from "@mui/material";
import ArrowForwardIosIcon from "@mui/icons-material/ArrowForwardIos";
import { NavLink } from "react-router";

const PLGHome = () => {
  const homeOptions = [
    {
      text: "Iniciar una simulación",
      link: "/simulacion",
    },
    {
      text: "Ver flota de camiones",
      link: "/camiones",
    }
  ];

  return (
    <>
      <Box
        width="100%"
        height="100%"
        margin="0px"
        padding="0px"
        alignContent="center"
        justifyContent="center"
        sx={{
          backgroundImage: `url("https://images.expertmarket.co.uk/wp-content/uploads/sites/2/2023/07/Best-Fleet-Management-Companies-Lead-image.jpg")`,
          backgroundRepeat: "no-repeat",
          backgroundSize: "cover",
          background:
            "linear-gradient(to down, #333, #333 50%, #eee 75%, #333 75%)",
        }}
      >
        <Stack>
          <Box
            sx={{
              width: "100%",
              height: "100%",
              backgroundColor: "rgba(0, 0, 0, 0.65)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              padding: "20px",
              paddingX: "50px",
            }}
          >
            <Typography
              variant="h2"
              color="white"
              textAlign="center"
              fontWeight="600"
            >
              Bienvenido a PLG Logistics <br />
              Solución de Gestión de Flotas
            </Typography>
          </Box>
        </Stack>
        <Stack
          margin="25px"
          padding="10px"
          display="flex"
          flexDirection="row"
          gap="100px"
          alignContent="center"
          justifyContent="center"
        >
          {homeOptions.map((item, idx) => (
            <Box
              key={idx}
              sx={{
                height: "250px",
                width: "250px",
                backgroundColor: "rgba(0, 0, 0, 0.65)",
                alignItems: "center",
                justifyContent: "center",
                color: "white",
                transition: "background 0.2s, color 0.2s",
                "&:hover": {
                  backgroundColor: "rgba(0, 28, 56, 0.61)", // azul con opacidad
                  color: "#fff",
                  cursor: "pointer",
                  "& .MuiTypography-root": {
                    color: "#fff",
                    fontWeight: "bold",
                  },
                  "& .MuiSvgIcon-root": {
                    color: "#fff",
                  },
                },
              }}
            >
              <NavLink
                style={{
                  width: "100%",
                  height: "100%",
                  textDecoration: "none",
                  color: "inherit",
                  alignItems: "center",
                  justifyContent: "center",
                  display: "flex",
                  flex: 1,
                  padding: "10px",
                }}
                to={item.link}
              >
                <Typography
                  variant="h5"
                  color="white"
                  textAlign="center"
                  fontWeight="400"
                >
                  {item.text}
                </Typography>
                <ArrowForwardIosIcon color="inherit"></ArrowForwardIosIcon>
              </NavLink>
            </Box>
          ))}
        </Stack>
      </Box>
    </>
  );
};

export default PLGHome;
