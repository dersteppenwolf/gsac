
set fp [open [lindex $argv 0] r]
set c [read $fp]
close $fp

set blobs [list]

set cnt 0
set div {\+----------------------\+}
set div2 {+----------------------+}
set pattern "$div\(.*?)$div\(.*?)$div\(.*)\$"
while {[regexp $pattern $c match header content rest]} {
    if {[regexp {^-+\+\s*$} $header]} {
	set c "$div2$content$div2$rest"
#	exit
	continue
    }

#    puts "HEADER:\n$header"
#    puts "CONTENT:\n$content"
    lappend blobs [list $header $content]
    set c "$div2$rest"
#    puts "*******\n$c"
    if {[incr cnt]>2} {
#	exit
    }
}

set whats [list dome antenna receiver]
array set fps {}
foreach var  $whats {
    set fps($var) [open site.$var.properties w]
}

foreach tuple $blobs {
    foreach {header content} $tuple break;
#    puts "Header: $header"
    set what ""
    if {[regexp -nocase {domes} $header]} {
	set what "dome"
    } elseif {[regexp {Antenna} $header]} {
	set what "antenna"
    } elseif {[regexp {Receivers} $header] || [regexp {Rcvr} $header]} {
	set what "receiver"
    } else {
	continue;
	puts "Huh? $header"
    }
    set fp $fps($what)
    set desc ""
    set prop ""
    foreach line [split $content "\n"] {
	set line [string trim $line]
	if {$line==""} {continue}
	if {[regexp {^------} $line]} {continue}
	set toks  [split $line |]
	set left [lindex $toks 1]
	set right [lindex $toks 2]
	set left [string trim $left]
	set right [string trim $right]
#	puts "left=$left"
#	puts "right=$right"
	if {$left == ""} {
	    append desc $right
	    append desc " "
	} else {
	    if {$prop !=""} {
		puts $fp "$prop = $desc"
	    }
	    set prop $left
	    regsub -all -nocase {xxxxxxxxxxxxxxx} $prop {} prop
	    set prop [string trim $prop]
	    set desc $right
	}
    }
    puts $fp "$prop = $desc"
##    puts "$prop = $desc"
}


foreach var  $whats {
    close $fps($var)
}






