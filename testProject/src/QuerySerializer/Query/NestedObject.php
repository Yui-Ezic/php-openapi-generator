<?php

namespace App\QuerySerializer\Query;

readonly class NestedObject
{
    public function __construct(
        public int $id,
        public string $value
    )
    {
    }
}