$repoDir = "C:\Users\uznpl\Downloads\SWP391_Finora-main"
$filePath = "src\main\java\dao\system\AuditLogDAO.java"
$fullPath = Join-Path $repoDir $filePath

Set-Location $repoDir

# Read all lines
$lines = [System.IO.File]::ReadAllLines($fullPath)
$total = $lines.Count
$lineNum = 1

foreach ($line in $lines) {
    # Re-read current file state
    $remaining = [System.IO.File]::ReadAllLines($fullPath)
    
    if ($remaining.Count -gt 1) {
        # Remove first line
        $newLines = $remaining[1..($remaining.Count - 1)]
        [System.IO.File]::WriteAllLines($fullPath, $newLines)
    } else {
        # Last line - write empty
        [System.IO.File]::WriteAllText($fullPath, "")
    }

    # Prepare commit message (truncate long lines)
    $msg = $line.Trim()
    if ($msg.Length -gt 50) { $msg = $msg.Substring(0, 47) + "..." }
    
    git add $filePath
    git commit -m "remove line ${lineNum}/${total}: $msg"
    
    $lineNum++
}

# Delete the empty file
git rm $filePath
git commit -m "delete empty AuditLogDAO.java"

Write-Host "Done! Removed all $total lines."
