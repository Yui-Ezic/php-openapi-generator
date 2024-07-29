<?php

namespace App\QuerySerializer\Transformer;

readonly class UrlEncode
{
    private const string RESERVED = "/?#[]@!$&'()*+,;=";

    public function __construct(private bool $allowReserved = false)
    {
    }

    public function __invoke(string $value, callable $next): string
    {
        return $this->encode($next());
    }

    private function encode(string $value): string
    {
        $encoded = '';

        $len = strlen($value);
        for ($i = 0; $i < $len; $i++) {
            if ($this->isShouldBeEncoded($value[$i])) {
                $encoded .= rawurlencode($value[$i]);
            } else {
                $encoded .= $value[$i];
            }
        }

        return $encoded;
    }

    private function isShouldBeEncoded(string $char): bool
    {
        if (str_contains(self::RESERVED, $char)) {
            return !$this->allowReserved;
        }
        return true;
    }
}