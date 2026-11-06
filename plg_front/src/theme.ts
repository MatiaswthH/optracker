import { createTheme } from "@mui/material/styles";

// A professional and clean color palette
const theme = createTheme({
    palette: {
        primary: {
            main: "#2c3e50", // A deep, muted blue
        },
        secondary: {
            main: "#3498db", // A brighter, friendly blue for accents
        },
        background: {
            default: "#ecf0f1", // A very light grey for the main background
            paper: "#ffffff", // White for cards and surfaces
        },
        text: {
            primary: "#2c3e50", // Dark grey for primary text
            secondary: "#7f8c8d", // Lighter grey for secondary text
        },
    },
    typography: {
        fontFamily: "Montserrat, sans-serif",
        h1: {
            fontWeight: 700,
            fontSize: "2.5rem",
        },
        h2: {
            fontWeight: 700,
            fontSize: "2rem",
        },
        h3: {
            fontWeight: 600,
            fontSize: "1.75rem",
        },
        body1: {
            fontSize: "1rem",
        },
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 8,
                    textTransform: "none",
                    fontWeight: 600,
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                    boxShadow: "0 4px 12px rgba(0,0,0,0.05)",
                },
            },
        },
        MuiAppBar: {
            styleOverrides: {
                root: {
                    backgroundColor: "#ffffff",
                    color: "#2c3e50",
                    boxShadow: "none",
                    borderBottom: "1px solid #bdc3c7"
                }
            }
        }
    },
});

export default theme;
