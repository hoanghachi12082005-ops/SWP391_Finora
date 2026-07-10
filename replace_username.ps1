$file = "c:\Users\uznpl\Downloads\SWP391_Finora-main\src\main\webapp\views\auth\login.jsp"
$content = Get-Content $file -Raw

# Replace the username-only block with username+password block
$oldBlock = [regex]::Escape(@"
        String username = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("username")) {
                    username = cookie.getValue();
                    break;
                }
            }
        }
"@)

$newBlock = @"
        String username = "";
        String password = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("username")) {
                    username = cookie.getValue();
                } else if (cookie.getName().equals("password")) {
                    password = cookie.getValue();
                }
            }
        }
"@

if ($content -match $oldBlock) {
    $content = $content -replace $oldBlock, $newBlock
    Set-Content $file $content -NoNewline -Encoding utf8
    Write-Output "login.jsp: Updated successfully"
} else {
    Write-Output "login.jsp: Pattern not found!"
}
