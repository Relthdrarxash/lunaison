// Agrandit une capture d'écran au clic, dans la page, sans ouvrir d'onglet.
// Clavier : Entrée ou Espace pour agrandir, Échap pour fermer.
(function () {
  var images = document.querySelectorAll('.gallery img');
  if (!images.length || typeof HTMLDialogElement !== 'function') return;

  var dialog = document.createElement('dialog');
  dialog.className = 'zoom';
  dialog.setAttribute('aria-label', 'Capture agrandie');
  dialog.innerHTML =
    '<button type="button" class="zoom-close" aria-label="Fermer">×</button>' +
    '<img alt=""><p class="zoom-caption"></p>';
  document.body.appendChild(dialog);
  var big = dialog.querySelector('img');
  var caption = dialog.querySelector('.zoom-caption');
  var opener = null;

  function open(img) {
    opener = img;
    big.src = img.currentSrc || img.src;
    big.alt = img.alt;
    var figcaption = img.closest('figure') && img.closest('figure').querySelector('figcaption');
    caption.textContent = figcaption ? figcaption.textContent : '';
    dialog.showModal();
  }

  function close() {
    dialog.close();
  }

  images.forEach(function (img) {
    img.tabIndex = 0;
    img.setAttribute('role', 'button');
    img.setAttribute('aria-label', 'Agrandir : ' + img.alt);
    img.addEventListener('click', function () { open(img); });
    img.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); open(img); }
    });
  });

  // Un clic n'importe où (image, fond, bouton) referme ; Échap est géré par le navigateur.
  dialog.addEventListener('click', close);
  dialog.addEventListener('close', function () { if (opener) opener.focus(); });
})();
