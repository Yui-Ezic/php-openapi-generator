<?php

namespace App\QuerySerializer\Query\SpaceDelimited;

use App\QuerySerializer\Transformer\SpaceDelimited\ArrayNoExplode;

readonly class NoExplode
{
    public function __construct(
        /** @var list<string> */
        #[ArrayNoExplode]
        public array $stringList,
    )
    {
    }
}