<?php

namespace App\QuerySerializer;

use CuyZ\Valinor\Normalizer\Normalizer;

final readonly class QuerySerializer
{
    private Normalizer $normalizer;

    public function __construct()
    {
        $this->normalizer = (new \CuyZ\Valinor\MapperBuilder())
            ->normalizer(\CuyZ\Valinor\Normalizer\Format::array());
    }

    public function serialize(object $query): string
    {
        // TODO: array & objects serialization
        // TODO: Support "allowReserved: true"
        $array = $this->normalizer->normalize($query);
        return http_build_query(data: $array, encoding_type: PHP_QUERY_RFC3986);
    }
}