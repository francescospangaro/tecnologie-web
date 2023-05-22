## Running
Use the IntelliJ 'Cargo Run' configuration or from the console:
```bash
maven clean package cargo:run
```
then navigate to `localhost:8080/webapp`.

To debug, use the IntelliJ 'Cargo Debug' configuration instead or from the console:
```bash
clean package cargo:run -Ddebug-cargo
```
then attach a remote debugger to port 5005 and navigate to `localhost:8080/webapp`.

`http://localhost:8080/host-manager` and `http://localhost:8080/manager`
can be accessed by using `root` and `password` as credentials.