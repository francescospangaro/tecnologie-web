document.addEventListener('DOMContentLoaded', async () => {
    const form = document.getElementById('login-form')
    const errorCred = document.getElementById('errorCred')
    const errorNotFound = document.getElementById('errorNotFound')

    const url = ""
    form.addEventListener('submit', async e => {
        e.preventDefault()

        if (!e.target.checkValidity()) {
            e.target.reportValidity()
            return
        }

        errorCred.setAttribute("hidden", "")
        errorNotFound.setAttribute("hidden", "")

        const response = await fetch(url + "userLogin", {
            body: new URLSearchParams(new FormData(e.target)),
            method: 'POST',
        });

        const res = await response.json()

        if (res.error) {
            if (res.msg === "errorCred") {
                errorCred.removeAttribute("hidden")
                return;
            }
            if (res.msg === "errorNotFound") {
                errorNotFound.removeAttribute("hidden")
                return;
            }
            console.error("Unexpected error", res)
            return;
        }

        localStorage.setItem('user', JSON.stringify(res))
        document.location = "home"
    })
});