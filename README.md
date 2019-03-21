This is my take on the coding exercise. I tried to keep the external Dependencies
to a minimum. However, I am using cats and cats-effect for FP typy things, http4s
for the http client and server, and circe for json parsing.

To run the tests type `sbt test`; to run the server type `sbt run`; 
This will bind the server to `localhost:8080`

Assumptions are commented at the relevant location in code.

If you have any questions or comments, feel free to ask and I will clarify.
