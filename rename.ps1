$ErrorActionPreference = 'Stop'

# 1. Reemplazo de texto en todo el proyecto
Write-Host "Reemplazando texto en archivos..."
$extensions = @('*.kt', '*.kts', '*.xml', '*.pro', '*.java', '*.md', '*.json')
Get-ChildItem -Path "C:\Users\Administrator\Ghostymusicy" -Include $extensions -Recurse -File | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match 'com\.arturo254\.opentune' -or $content -match 'OpenTune' -or $content -match 'opentune') {
        $newContent = $content -replace 'com\.arturo254\.opentune', 'com.dieghosty10.ghostymusicy'
        $newContent = $newContent -replace 'opentune\.debug', 'ghostymusicy.debug'
        $newContent = $newContent -replace 'opentune', 'ghostymusicy'
        $newContent = $newContent -replace 'OpenTune', 'Ghostymusicy'
        $newContent = $newContent -replace 'Arturo254', 'Dieghosty10'
        Set-Content -Path $_.FullName -Value $newContent -NoNewline
    }
}

# 2. Renombrar directorios (de abajo hacia arriba para evitar problemas con rutas cambiando)
Write-Host "Renombrando directorios..."
$dirs = Get-ChildItem -Path "C:\Users\Administrator\Ghostymusicy" -Recurse -Directory | Where-Object { $_.Name -eq 'opentune' } | Sort-Object -Property FullName -Descending
foreach ($dir in $dirs) {
    if ($dir.Parent.Name -eq 'arturo254') {
        $newPath = Join-Path -Path $dir.Parent.FullName -ChildPath 'ghostymusicy'
        Rename-Item -Path $dir.FullName -NewName 'ghostymusicy'
        Rename-Item -Path $dir.Parent.FullName -NewName 'dieghosty10'
    }
}

Write-Host "¡Limpieza completada!"
