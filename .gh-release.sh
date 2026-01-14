#!/bin/bash

# Назва проекту
projectName=$1

# Версія проекту
projectVersion=$2

# Виводимо інформаційне повідомлення
echo Creating GitHub release v$projectVersion for \"$projectName\" project

# Перевіряємо, чи у CHANGELOG.md є оновлені зміни
if ! head -n 1 "CHANGELOG.md" | grep -qx "# $projectVersion"; then
    echo "Error: Changelog first line mast be \"# $projectVersion\"" >&2
    exit 1
fi

# Отримуємо повідомлення останнього коміту. 
commit_message=$(git log -1 --pretty=format:%s)

# Перевіряємо коректність релізного коміту
if [[ "$commit_message" != "Release v$projectVersion" ]]; then
  # Якщо останній коміт не релізний - виводимо помилку
  echo "Error: The last commit must be \"Release v$projectVersion\"" >&2
  # Завершуємо скрипт із кодом помилки
  exit 1
fi

# Отримуємо останні дані з віддаленого репозиторію без об'єднання
# '>/dev/null 2>&1' перенаправляє вивід та помилки в нікуди
git fetch >/dev/null 2>&1

# Отримуємо SHA-ідентифікатор локального коміту (HEAD)
local_sha=$(git rev-parse HEAD)

# Отримуємо SHA-ідентифікатор останнього коміту із віддаленої гілки
remote_sha=$(git rev-parse @{u})

# Перевіряємо, чи останній коміт знаходиться у віддаленому репозиторії
if [[ "$local_sha" != "$remote_sha" ]]; then
  echo "Error: The last commit has not been pushed to the remote repository" >&2
  exit 1
fi

# Заголовок релізу
titleMessage="${projectName//_/ } v$projectVersion"

# Тег для швидкого переходу по changelog'у
tagForNotes="${projectVersion//./}"

# Опис релізу
notesMessage="Full Changelog: [v$projectVersion](https://github.com/RutarAndriy/$projectName/blob/main/CHANGELOG.md#$tagForNotes)"

# Шлях до зібраного jar-файлу
jarDef="./target/$projectName-$projectVersion.jar"

# Помилок немає - створюємо GitHub-реліз
gh release create $projectVersion \
         --title "$titleMessage" \
         --notes "$notesMessage" $jarDef

# Імпортуємо нові теги
git pull

