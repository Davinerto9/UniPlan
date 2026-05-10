# Justificación de Base de Datos NoSQL: MongoDB vs Otras opciones

De acuerdo con las necesidades planteadas para **UniPlan**, la elección de **MongoDB** (una base de datos NoSQL orientada a documentos) es altamente adecuada y, de hecho, la mejor opción frente a otras alternativas (como Key-Value, Column-Family o Grafos) por las siguientes razones:

## 1. Flexibilidad de Esquema (Polimorfismo de Eventos)
El requerimiento principal menciona que existen diferentes tipos de eventos (Talleres, Torneos Deportivos, Voluntariados, Charlas) y que "otros eventos podrán contener información adicional no prevista inicialmente". 
- Una base de datos relacional requeriría un esquema complejo (Single Table con muchos nulos, o Class Table Inheritance con muchos JOINs). 
- Una base de datos **orientada a documentos (MongoDB)** permite que la colección `Events` tenga documentos con estructuras variables. Un documento de `Taller` puede tener un subdocumento de `prerequisites`, mientras que un documento de `Torneo` puede tener subdocumentos sobre `teams` y `rules`, todo en la misma colección y sin afectar el rendimiento de lectura.

## 2. Lecturas rápidas y completas
Dado que "el sistema tenga alta disponibilidad [...] y las consultas de eventos se realicen sin demoras perceptibles", la estructura de documentos embebidos de MongoDB (como embeber la `location`, `schedule`, y `capacity` dentro del mismo documento del evento) evita los costosos JOINs. La información necesaria para mostrar en la interfaz de usuario se extrae en una sola operación de lectura por documento.

## 3. Integración Ágil
El formato nativo de intercambio web es JSON. MongoDB almacena los datos en BSON, lo cual hace que la serialización/deserialización desde el backend (Spring Boot / Java) al Frontend (HTML/JS) sea sumamente natural y rápida.

## ¿Por qué no otras soluciones NoSQL?
- **Key-Value (ej. Redis/DynamoDB):** Excelentes para cachés, pero muy limitados para realizar consultas complejas o filtrados (ej. "filtrar eventos por tipo, rango de fechas y estado").
- **Column-Family (ej. Cassandra):** Ideales para Big Data y series de tiempo escribiendo datos a altísima velocidad, pero su modelo de modelado por consultas ("Query-driven modeling") dificultaría la flexibilidad requerida para agregar nuevos atributos a los eventos en el futuro sin rediseñar las tablas.
- **Grafos (ej. Neo4j):** Muy útiles para redes sociales (relaciones de "quién conoce a quién"), pero en este dominio transaccional de registro a eventos, la complejidad de grafos es innecesaria y añadiría un overhead tecnológico que no aporta valor a las inscripciones.

## Conclusión
Por lo tanto, **MongoDB** satisface perfectamente la necesidad de **esquemas flexibles (variabilidad de eventos)**, **baja latencia (lectura de documentos agregados)** y **escalabilidad horizontal**, complementando a la perfección la base relacional institucional que mantiene los datos académicos estructurados y estáticos.
