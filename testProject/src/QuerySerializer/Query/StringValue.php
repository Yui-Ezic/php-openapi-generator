<?php

namespace App\QuerySerializer\Query;

readonly class StringValue
{
    public function __construct(
        public string $value
    )
    {
    }
}