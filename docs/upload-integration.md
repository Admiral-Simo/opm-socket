# Intégration de l'envoi de fichiers et résolution du 404

## 🎯 Objectif
Expliquer en détail le flux d'envoi de fichiers (du frontend au backend), le diagnostic du problème 404 lors de l'ouverture d'un fichier uploadé, et documenter les modifications apportées pour résoudre le problème.

---

## Vue d'ensemble du flux (end-to-end)
1. L'utilisateur sélectionne un fichier dans l'interface de chat (frontend).
2. Le frontend crée un `FormData` et POSTe vers `http://<BACKEND>/upload` (endpoint de backend).
3. Le backend reçoit le fichier via `UploadController`, le stocke sur disque dans le dossier `uploads/` et renvoie une réponse JSON contenant `url: "/uploads/<filename>"`.
4. Le frontend récupère cette `url` et l'envoie comme contenu d'un message via WebSocket afin que tous les participants du chat puissent accéder au fichier.
5. Quand un utilisateur clique sur le lien ou quand on affiche une image, le navigateur doit charger l'URL complète pointant vers le backend (par ex. `http://localhost:8080/uploads/<filename>`).

---

## Problème rencontré
- Comportement observé : en cliquant sur un lien `/uploads/<file>` depuis l'interface (Next.js), la barre d'adresse passe à `http://localhost:3000/uploads/<file>` et la page renvoie `404`.
- Cause racine : le message contenait une URL relative (`/uploads/...`). Comme la page courante est servie par Next.js (`localhost:3000`), le navigateur essaie de charger `localhost:3000/uploads/...`. Or Next.js ne sert pas le fichier (le backend doit le servir), d'où le 404.

---

## Ce que nous avons modifié
**Fichier frontend modifié :**
- `frontend/app/(protected)/chat/page.tsx`
  - Ajout d'une fonction utilitaire `getAbsoluteUrl(url)` qui préfixe automatiquement les chemins qui commencent par `/uploads/` avec la variable d'environnement `NEXT_PUBLIC_API_URL`.
  - Utilisation de `getAbsoluteUrl(...)` pour :
    - `img src` (affichage d'images uploadées)
    - `a href` (liens vers fichiers)

**Pourquoi :** cela garantit que les liens pointent explicitement vers le backend (ex. `http://localhost:8080/uploads/...`) et non vers le domaine du frontend.

Aucun changement nécessaire côté backend pour la résolution de ce 404 (les endpoints et la configuration existe déjà) ; toutefois, les composants backend impliqués sont :
- `UploadController.java` : reçoit et sauvegarde les fichiers, retourne `{"url": "/uploads/<file>"}`.
- `WebMvcConfig.java` : sert les fichiers statiques depuis `file:uploads/` sous la route `/uploads/**`.
- `SecurityConfig.java` : autorise l'accès public à `/uploads/**`.

---

## Variables d'environnement
- **`NEXT_PUBLIC_API_URL`** (frontend) : base URL du backend (ex. `http://localhost:8080`).
  - Méthode recommandée (local) : créer `frontend/.env.local` avec :

```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## Comment tester
1. Lancer le backend (mvn spring-boot:run ou via Docker selon votre setup).
2. Lancer le frontend (`npm run dev` dans `frontend`).
3. Ouvrir la page de chat, uploader un fichier.
4. Vérifier :
   - Le message envoyé contient `content` égal à `/uploads/<filename>` (côté serveur) ou que l'affichage montre un lien.
   - En cliquant sur le lien, la barre d'adresse doit devenir `http://localhost:8080/uploads/<filename>` (et le fichier s'affiche/télécharge).
   - Les images doivent s'afficher inline dans le chat.

---

## Débogage & cas courants
- Si le fichier retourne toujours 404 :
  - Vérifier que le backend est bien démarré sur le port attendu et que `NEXT_PUBLIC_API_URL` pointe vers ce port.
  - Vérifier que les fichiers existent physiquement dans le dossier `uploads/` à la racine du backend.
  - Vérifier `WebMvcConfig` et `SecurityConfig` pour s'assurer que `/uploads/**` est correctement servi et autorisé.

---

## Tests existants
- `backend/src/test/java/.../UploadControllerTest.java` vérifie l'upload et que la réponse JSON contient un `url` commençant par `/uploads/`.

---

## Résumé rapide ✅
- Problème : liens uploadés relatifs servis par Next.js → 404.
- Solution : préfixer les liens `/uploads/` côté frontend avec `NEXT_PUBLIC_API_URL` (utilitaire `getAbsoluteUrl`).

---

## Fichiers modifiés/ajoutés
- Modifié : `frontend/app/(protected)/chat/page.tsx` (ajout de `getAbsoluteUrl` + utilisation pour `img`/`a`).
- Ajouté : `docs/upload-integration.md` (ce document).

---

Si tu veux, je peux aussi :
- ajouter un test côté frontend qui vérifie que `getAbsoluteUrl` renvoie bien l'URL complète, ou
- modifier l'affichage pour n'afficher que le nom du fichier (au lieu de l'URL complète) et ajouter un clip `download`.

Souhaites-tu que j'ajoute l'un de ces éléments ?