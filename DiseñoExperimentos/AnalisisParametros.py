import pandas as pd
import os
import statsmodels.api as sm
from statsmodels.formula.api import ols
import seaborn as sns
from scipy.stats import shapiro

# Transformación de los datos para el análisis de varianza
# Se codifican los factores en -1 y +1 para el análisis de varianza
def codificar_factor(columna):
    return columna.map({min(columna): -1, max(columna): +1})

def fetch_and_process_data(file_path, file_path2):

    # ----------------- ACO ------------------
    # Read the CSV file
    df = pd.read_csv(file_path, sep=',',names=["n_ants", "n_iter", "alpha", "beta", "rho","execution_time_ms"])

    # Codificar los factores
    for col in df.columns[:-1]:
        df[col] = codificar_factor(df[col])

    #Modelo
    modelo = ols('execution_time_ms ~ n_ants * n_iter * alpha * beta * rho', data=df).fit()
    # Análisis de varianza (ANOVA)
    anova = sm.stats.anova_lm(modelo, typ=2)
    # Se ordena el ANOVA por pvalue
    anova_ordenado = anova.sort_values(by='PR(>F)', ascending=True)
    print(anova_ordenado) # Uncomment to see the ANOVA table

    muA = df.groupby('n_ants')['execution_time_ms'].mean()
    muB = df.groupby('n_iter')['execution_time_ms'].mean()
    muC = df.groupby('alpha')['execution_time_ms'].mean()
    muD =  df.groupby('beta')['execution_time_ms'].mean()

    print("=======================RESULTADOS ACO================================\n")
    print(f"Media de tiempos de ejecución por número de hormigas:\n {muA}\n")
    print(f"Media de tiempos de ejecución por número de iteraciones:\n {muB}\n")
    print(f"Media de tiempos de ejecución por alpha:\n {muC}\n")
    print(f"Media de tiempos de ejecución por beta:\n {muD}\n")

    #Verificar normalidad de los errores
    errores = modelo.resid

    stat, p_value = shapiro(errores)

    print('Estadístico:', stat)
    print('P-valor:', p_value)

    if p_value > 0.05:
        print('No se rechaza normalidad (los errores parecen normales)')
    else:
        print('Se rechaza normalidad (los errores no son normales)')

    # ----------------- GA ------------------
    # Read the CSV file
    df = pd.read_csv(file_path2, sep=',',names=["pop_size", "n_generations", "crossover", "mutation","execution_time_ms"])

    # Codificar los factores
    for col in df.columns[:-1]:
        df[col] = codificar_factor(df[col])

    #Modelo
    modelo2 = ols('execution_time_ms ~ pop_size * n_generations * crossover * mutation', data=df).fit()
    # Análisis de varianza (ANOVA)
    anova2 = sm.stats.anova_lm(modelo2, typ=2)
    # Se ordena el ANOVA por pvalue
    anova_ordenado2 = anova2.sort_values(by='PR(>F)', ascending=True)
    print(anova_ordenado2) # Uncomment to see the ANOVA table

    muA = df.groupby('pop_size')['execution_time_ms'].mean()
    muB = df.groupby('n_generations')['execution_time_ms'].mean()
    muAB = df.groupby(['pop_size', 'n_generations'])['execution_time_ms'].mean()

    print("=======================RESULTADOS GA================================\n")
    print(f"Media de tiempos de ejecución por poblacion:\n {muA}\n")
    print(f"Media de tiempos de ejecución por número de generaciones:\n {muB}\n")
    print(f"Media de tiempos de ejecución por poblacion * numero de generaciones:\n {muAB}\n")

    #Verificar normalidad de los errores
    errores2 = modelo2.resid

    stat, p_value = shapiro(errores2)

    print('Estadístico:', stat)
    print('P-valor:', p_value)

    if p_value > 0.05:
        print('No se rechaza normalidad (los errores parecen normales)')
    else:
        print('Se rechaza normalidad (los errores no son normales)')



if __name__ == "__main__":
    fetch_and_process_data("data\\resultados\\resultadosACO.csv","data\\resultados\\resultadosGA.csv")