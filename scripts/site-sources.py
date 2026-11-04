#!/usr/bin/env python3
"""Génère docs/sources.html à partir de documentation/sources.md.

Le fichier Markdown reste la seule source : on le modifie, puis on relance
ce script pour que la page du site dise exactement la même chose.

    python3 scripts/site-sources.py
"""
import html
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "documentation" / "sources.md"
OUT = ROOT / "docs" / "sources.html"


def inline(text: str) -> str:
    text = html.escape(text, quote=False)
    # Liens relatifs vers le dépôt : on pointe vers GitHub.
    def link(m):
        label, url = m.group(1), m.group(2)
        if not url.startswith("http"):
            url = "https://github.com/Relthdrarxash/lunaison/blob/HEAD/documentation/" + url
        return f'<a href="{html.escape(url)}">{label}</a>'
    text = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", link, text)
    text = re.sub(r"\*\*(.+?)\*\*", r"<strong>\1</strong>", text)
    text = re.sub(r"\*(.+?)\*", r"<em>\1</em>", text)
    text = re.sub(r"`(.+?)`", r"<code>\1</code>", text)
    return text


def convert(md: str) -> tuple[str, str]:
    out, title = [], ""
    lines = md.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i]
        if line.startswith("# "):
            title = line[2:].strip()
            out.append(f"<h1>{inline(title)}</h1>")
        elif line.startswith("## "):
            out.append(f"<h2>{inline(line[3:].strip())}</h2>")
        elif line.startswith("|"):
            rows = []
            while i < len(lines) and lines[i].startswith("|"):
                rows.append([c.strip() for c in lines[i].strip().strip("|").split("|")])
                i += 1
            head, body = rows[0], [r for r in rows[2:]]
            out.append('<div class="table-scroll"><table>')
            out.append("<thead><tr>" + "".join(f"<th>{inline(c)}</th>" for c in head) + "</tr></thead>")
            out.append("<tbody>")
            for r in body:
                out.append("<tr>" + "".join(f"<td>{inline(c)}</td>" for c in r) + "</tr>")
            out.append("</tbody></table></div>")
            continue
        elif line.startswith("- "):
            out.append("<ul>")
            while i < len(lines) and lines[i].startswith("- "):
                out.append(f"<li>{inline(lines[i][2:])}</li>")
                i += 1
            out.append("</ul>")
            continue
        elif line.strip():
            out.append(f"<p>{inline(line.strip())}</p>")
        i += 1
    return title, "\n      ".join(out)


TEMPLATE = """<!doctype html>
<html lang="fr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>{title} | Lunaison</title>
  <meta name="description" content="D'où viennent les repères de Lunaison, et pourquoi l'appli est faite ainsi : références scientifiques et choix de conception.">
  <link rel="icon" href="icone.svg" type="image/svg+xml">
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <!-- Page générée par scripts/site-sources.py à partir de documentation/sources.md : ne pas la modifier à la main. -->
  <a class="skip" href="#contenu">Aller au contenu</a>
  <header class="site-header">
    <div class="wrap">
      <a class="brand" href="./">
        <svg viewBox="0 0 24 24" fill="none" stroke="#F1E2EA" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8Z"/></svg>
        Lunaison
      </a>
      <nav class="site-nav" aria-label="Navigation du site">
        <a href="./#fonctions">L'appli</a>
        <a href="sources.html" aria-current="page">Sources et choix</a>
        <a href="confidentialite.html">Confidentialité</a>
      </nav>
    </div>
  </header>
  <main id="contenu" class="wrap">
    <article class="prose prose-wide">
      {body}
      <p>Une remarque sur une source, un seuil ou une formulation ? Écrivez à <a href="mailto:escale.apps@proton.me">escale.apps@proton.me</a> ou ouvrez une demande sur <a href="https://github.com/Relthdrarxash/lunaison/issues">GitHub</a>.</p>
    </article>
  </main>
  <footer class="site-footer">
    <div class="wrap">
      <p>Lunaison n'est ni un dispositif médical ni une méthode de contraception.</p>
      <div class="footer-links">
        <a href="./">Accueil</a>
        <a href="sources.html">Sources et choix</a>
        <a href="confidentialite.html">Confidentialité</a>
        <a href="https://github.com/Relthdrarxash/lunaison">Code source</a>
        <a href="mailto:escale.apps@proton.me">Contact</a>
      </div>
    </div>
  </footer>
</body>
</html>
"""

if __name__ == "__main__":
    md = SRC.read_text(encoding="utf-8")
    # La phrase qui renvoie vers cette page n'a pas de sens sur la page elle-même.
    md = re.sub(r" Il est publié sur le site : \[[^\]]*\]\([^)]*\)\.", "", md)
    title, body = convert(md)
    OUT.write_text(TEMPLATE.format(title=html.escape(title), body=body), encoding="utf-8")
    print(f"{OUT.relative_to(ROOT)} généré")
