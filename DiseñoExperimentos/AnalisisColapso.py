import pandas as pd
from scipy.stats import shapiro, levene, ttest_ind, mannwhitneyu
import pandas as pd
from scipy.stats import shapiro, levene, ttest_ind, mannwhitneyu

def fetch_and_process_data(file_path, file_path2):

    # Cargar datos
    genetico = pd.read_csv(file_path)
    ant_colony = pd.read_csv(file_path2, names=["tiempo_colapso"])

    # 1. Test de normalidad
    print(shapiro(genetico))
    print(shapiro(ant_colony))

    # 2. Test de varianzas
    print(levene(genetico, ant_colony))

    # Calcular medianas
    mediana_genetico = genetico.median()
    mediana_ant_colony = ant_colony.median()

    # 3. Elección de prueba
    if (shapiro(genetico).pvalue > 0.05 and shapiro(ant_colony).pvalue > 0.05) and (levene(genetico, ant_colony).pvalue > 0.05):
        print("Ambos algoritmos son normales y tienen varianzas iguales. Se usará t-test.")
        # Datos normales y varianzas iguales
        statistic, p_value = print(ttest_ind(genetico, ant_colony))

        # Interpretar resultado
        if p_value < 0.05:
            print("El p-value del t-test es menor a 0.05. Existe una diferencia significativa entre las medianas de los algoritmos.")
        else:
            print("No hay evidencia suficiente para afirmar que las medianas son diferentes.")

        print(f"Mediana de horas hasta el colapso del Algoritmo Genético: {mediana_genetico}")
        print(f"Mediana de horas hasta el colapso del Ant Colony: {mediana_ant_colony}")
    
    else:
        print("Los algoritmos no son normales o tienen varianzas distintas. Se usará Mann-Whitney U test.")
        # Datos no normales o varianzas distintas
        statistic, p_value =  mannwhitneyu(genetico, ant_colony)
        # Interpretar resultado
        if p_value < 0.05:
            print("El p-value del Mann-Whitney U es menor a 0.05. Existe una diferencia significativa entre las medianas de los algoritmos.")
        else:
            print("No hay evidencia suficiente para afirmar que las medianas son diferentes.")

        print(f"Mediana de horas hasta el colapso del Algoritmo Genético: {mediana_genetico}")
        print(f"Mediana de horas hasta el colapso del Ant Colony: {mediana_ant_colony}")

    

if __name__ == "__main__":
    fetch_and_process_data("data\\resultados\\resultadosColapsoLogisticoGA.csv","data\\resultados\\resultadosColapsoLogisticoACO.csv")