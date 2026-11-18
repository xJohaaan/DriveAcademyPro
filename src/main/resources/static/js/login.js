// Botones de acceso rápido: autocompletan email y contraseña de los usuarios de prueba.
// Un clic rellena el formulario; doble clic rellena e inicia sesión.
document.addEventListener('DOMContentLoaded', function () {
    var email = document.getElementById('email');
    var password = document.getElementById('password');
    var form = document.getElementById('login-form');

    document.querySelectorAll('[data-demo-email]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            email.value = btn.getAttribute('data-demo-email');
            password.value = '123456';
            document.body.style.setProperty('--accent', btn.getAttribute('data-accent'));
        });
        btn.addEventListener('dblclick', function () {
            form.submit();
        });
    });
});
