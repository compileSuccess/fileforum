<?php
$file = '../posts.txt';
$posts = nl2br(file_get_contents('php://input'));
$posts .= file_get_contents($file);
file_put_contents($file, $posts);
?>