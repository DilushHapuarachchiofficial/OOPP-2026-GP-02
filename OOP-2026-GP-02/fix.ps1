$path = "src/main/java/model/Lecturer/CourseMaterialsLogic.java"
$lines = [System.IO.File]::ReadAllLines($path)
$out = [System.Collections.Generic.List[string]]::new()
for ($i = 0; $i -lt $lines.Length; $i++) {
    if ($i -ge 774 -and $i -le 776) {
        continue
    }
    $out.Add($lines[$i])
}
[System.IO.File]::WriteAllLines($path, $out)
Write-Host "SUCCESS: Removed lines 774-776, new length: $($out.Count)"

