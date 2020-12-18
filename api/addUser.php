<?php
$data = file_get_contents('php://input');
//parse_str($_POST, $data);
$username = explode("=", (explode("&", $data)[0]))[1];
$password = explode("=", (explode("&", $data)[1]))[1];

$userpath = "../users/";
$userpath .= $username;
$userpath .= "/";

if (is_dir($userpath)) {
	throw new Exception("Username already taken.");
}
mkdir($userpath, 0700);

$passpath = $userpath;
$passpath .= $password;
$passpath .= ".txt";
file_put_contents($passpath, "asdf");
?>