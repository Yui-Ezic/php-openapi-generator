<?php

namespace App\QuerySerializer\Transformer\Form;

use Attribute;
use CuyZ\Valinor\Normalizer\AsTransformer;

#[AsTransformer]
#[Attribute(Attribute::TARGET_PROPERTY)]
class ArrayNoExplode
{
    /**
     * @param non-empty-list $array
     */
    public function normalize(array $array, callable $next): string
    {
        return implode(',', $next());
    }
}