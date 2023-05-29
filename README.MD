## Running
Use the IntelliJ `Cargo Run HTML`/`Cargo Run JS` configuration or from the console:
```bash
maven clean package cargo:run -Dcargo-run-html-pure
```
```bash
maven clean package cargo:run -Dcargo-run-js
```
then navigate to `localhost:8080/HTMLPure/`/`localhost:8081/JS/home.html`.

To debug, use the IntelliJ `Cargo Debug HTML`/`Cargo Debug JS` configuration instead or from the console:
```bash
maven clean package cargo:run -Dcargo-run-html-pure -Ddebug-cargo
```
```bash
maven clean package cargo:run -Dcargo-run-js -Ddebug-cargo
```
then attach a remote debugger to port 5005/5006 and navigate to `localhost:8080/HTMLPure/`/`localhost:8081/JS/home.html`.

`http://localhost:8080/host-manager` and `http://localhost:8080/manager`
can be accessed by using `root` and `password` as credentials.