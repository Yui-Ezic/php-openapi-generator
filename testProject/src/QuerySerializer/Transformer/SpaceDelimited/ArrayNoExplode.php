<?php

namespace App\QuerySerializer\Transformer\SpaceDelimited;

use Attribute;
use CuyZ\Valinor\Normalizer\AsTransformer;

#[AsTransformer]
#[Attribute(Attribute::TARGET_PROPERTY)]
class ArrayNoExplode
{
    private const string ENCODED_SPACE = '%20';

    /**
     * @param non-empty-list $array
     */
    public function normalize(array $array, callable $next): string
    {
        return implode(self::ENCODED_SPACE, $array);
    }
}